package com.lex.simplequest.data.location.repository

import android.content.Context
import android.database.ContentObservable
import android.database.ContentObserver
import android.location.LocationManager
import android.net.Uri
import android.util.Log
import androidx.core.util.Preconditions
import com.lex.core.utils.MainThreadHandler
import com.lex.core.utils.ignoreErrors
import com.lex.simplequest.device.content.provider.QuestContract
import com.lex.simplequest.domain.model.Track
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
            MainThreadHandler.instance().postDelayed(delayedNotificationRunnable, NOTIFICATION_DELAY_MS)
        }

        override fun onChange(selfChange: Boolean) {
            this.onChange(selfChange, null)
        }

        override fun deliverSelfNotifications(): Boolean = true
    }

    init {

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

    override fun startTrack(name: String): Long {
        checkNotClosed()
        val track = Track(-1, name, System.currentTimeMillis(), null)
        val uri = context.contentResolver.insert(QuestContract.Tracks.CONTENT_URI, track.toContentValues())
        return 0L // Temporary
    }

    override fun stopTrack(id: Long) {
        TODO("Not yet implemented")
    }

    override fun getTrack(id: Long): Track? {
        TODO("Not yet implemented")
    }

    override fun updateTrack(track: Track): Boolean {
        TODO("Not yet implemented")
    }

    override fun deleteTrack(id: Long): Boolean {
        TODO("Not yet implemented")
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
        //Preconditions.checkState(!isClosed.get(), "This instance is closed")
        if (isClosed.get()) {
            Log.e("qaz", "This instance is closed")
        }
    }
}