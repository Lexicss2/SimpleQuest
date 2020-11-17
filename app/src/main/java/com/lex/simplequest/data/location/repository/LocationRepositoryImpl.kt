package com.lex.simplequest.data.location.repository

import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.location.LocationManager
import android.net.Uri
import android.util.Log
import androidx.core.database.getDoubleOrNull
import com.lex.core.utils.MainThreadHandler
import com.lex.core.utils.ignoreErrors
import com.lex.simplequest.data.location.repository.queries.AllTracksQuerySpecification
import com.lex.simplequest.data.location.repository.queries.LatestTrackQuerySpecification
import com.lex.simplequest.data.location.repository.queries.TrackByIdQuerySpecification
import com.lex.simplequest.data.location.repository.queries.TrackByNameQuerySpecification
import com.lex.simplequest.device.content.provider.QuestContract
import com.lex.simplequest.domain.model.Point
import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.domain.model.distance
import com.lex.simplequest.domain.repository.LocationRepository
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

class LocationRepositoryImpl(ctx: Context) : LocationRepository {
    private val nativeLocationManager =
        ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    companion object {
        private const val NOTIFICATION_DELAY_MS = 300L
    }

    private val context = ctx
    private val listeners = CopyOnWriteArrayList<LocationRepository.OnUpdateListener>()
    private val isClosed = AtomicBoolean(false)

    private val contentObserver = object : ContentObserver(MainThreadHandler.instance()) {
        private val delayedNotificationRunnable = object : Runnable {
            override fun run() {
                notifyAlertsUpdated()
            }
        }

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            MainThreadHandler.instance().removeCallbacks(delayedNotificationRunnable)
            MainThreadHandler.instance()
                .postDelayed(delayedNotificationRunnable, NOTIFICATION_DELAY_MS)
        }

        override fun onChange(selfChange: Boolean) {
            this.onChange(selfChange, null)
        }

        override fun deliverSelfNotifications(): Boolean = true
    }

    private val querySpecFactory: LocationRepository.LocationQuerySpecificationFactory by lazy {
        object : LocationRepository.LocationQuerySpecificationFactory {
            override fun trackById(trackId: Long): LocationRepository.LocationQuerySpecification =
                TrackByIdQuerySpecification(trackId)

            override fun trackByName(trackName: String): LocationRepository.LocationQuerySpecification =
                TrackByNameQuerySpecification(trackName)

            override fun allTracks(): LocationRepository.LocationQuerySpecification =
                AllTracksQuerySpecification()

            override fun latestTrack(): LocationRepository.LocationQuerySpecification =
                LatestTrackQuerySpecification()
        }
    }

    init {
        context.contentResolver.registerContentObserver(
            QuestContract.Tracks.CONTENT_URI,
            true,
            contentObserver
        )
    }

    override fun isLocationServicesEnabled(): Boolean {
        val isGpsEnabled = ignoreErrors(false) {
            nativeLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }
        val isNetworkEnabled = ignoreErrors(false) {
            nativeLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }

        return isGpsEnabled || isNetworkEnabled
    }

    override fun startTrack(name: String, startTime: Long): Long {
        checkNotClosed()
        val track = Track(-1, name, startTime, null)
        context.contentResolver.insert(
            QuestContract.Tracks.CONTENT_URI,
            track.toContentValues()
        )
        val readTrack = getTracks(TrackByNameQuerySpecification(track.name)).first()
        return readTrack.id
    }

    override fun stopTrack(id: Long, endTime: Long, minimalDistance: Long?): Boolean {
        checkNotClosed()
        val spec = TrackByIdQuerySpecification(id)
        val readTrack = getTracks(spec).first()
        val trackDistance = readTrack.distance()
        val keepTrack = null == minimalDistance || trackDistance >= minimalDistance
        if (keepTrack) {
            val updatedTrack = readTrack.copy(endTime = endTime)
            val updatedRows = context.contentResolver.update(
                QuestContract.Tracks.CONTENT_URI,
                updatedTrack.toContentValues(),
                (spec as LocationQuerySpecificationImpl).getWhereClause(),
                null
            )
            Log.d("qaz", "updatedRows = $updatedRows")
            return updatedRows > 0
        } else {
            // remove track
            context.contentResolver.delete(
                QuestContract.Tracks.CONTENT_URI,
                (spec as LocationQuerySpecificationImpl).getWhereClause(),
                null
            )
            Log.w("qaz", "track was no saved as it distance is $trackDistance rather low")
            return false
        }
    }

    override fun getTracks(spec: LocationRepository.LocationQuerySpecification): List<Track> {
        checkNotClosed()
        return context.contentResolver.query(
            QuestContract.Tracks.CONTENT_URI,
            null,
            (spec as LocationQuerySpecificationImpl).getWhereClause(),
            null,
            QuestContract.Tracks.COLUMN_START_TIME + " DESC"
        )?.use { cursor ->
            cursor.toFullTracks(context)
        } ?: emptyList()
    }

    override fun updateTrack(track: Track): Boolean {
        TODO("Not yet implemented")
    }

    override fun deleteTrack(id: Long): Boolean {
        checkNotClosed()
        val spec = TrackByIdQuerySpecification(id)
        val rowsCount = context.contentResolver.delete(
            QuestContract.Tracks.CONTENT_URI,
            (spec as LocationQuerySpecificationImpl).getWhereClause(),
            null
        )
        return rowsCount > 0
    }

    override fun addPoint(
        trackId: Long,
        latitude: Double,
        longitude: Double,
        altitude: Double?
    ) {
        checkNotClosed()
        val point = Point(-1, trackId, latitude, longitude, altitude, System.currentTimeMillis())
        context.contentResolver.insert(QuestContract.Points.CONTENT_URI, point.toContentValues())
        // Not necessary to read the inserted point
    }

    override fun getQuerySpecificationFactory(): LocationRepository.LocationQuerySpecificationFactory {
        checkNotClosed()
        return querySpecFactory
    }

    private fun notifyAlertsUpdated() {
        listeners.forEach { it.onUpdated() }
    }

    override fun close() {
        if (!isClosed.getAndSet(true)) {
            context.contentResolver.unregisterContentObserver(contentObserver)
        }
    }

    private fun checkNotClosed() {
        if (isClosed.get()) {
            Log.e("qaz", "This instance is closed")
        }
    }
}