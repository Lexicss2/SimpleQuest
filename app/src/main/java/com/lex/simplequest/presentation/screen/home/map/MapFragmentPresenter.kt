package com.lex.simplequest.presentation.screen.home.map

import android.util.Log
import com.lex.core.log.LogFactory
import com.lex.simplequest.data.location.repository.queries.LatestTrackQuerySpecification
import com.lex.simplequest.domain.common.connectivity.InternetConnectivityTracker
import com.lex.simplequest.domain.locationmanager.LocationTracker
import com.lex.simplequest.domain.locationmanager.model.Location
import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.domain.track.interactor.ReadTracksInteractor
import com.lex.simplequest.presentation.base.BaseMvpPresenter
import com.lex.simplequest.presentation.screen.home.MainRouter
import com.lex.simplequest.presentation.utils.asRxSingle
import com.lex.simplequest.presentation.utils.tasks.SingleResultTask
import io.reactivex.android.schedulers.AndroidSchedulers

class MapFragmentPresenter(
    private val readTracksInteractor: ReadTracksInteractor,
    internetConnectivityTracker: InternetConnectivityTracker,
    logFactory: LogFactory,
    router: MainRouter
) : BaseMvpPresenter<MapFragmentContract.Ui, MapFragmentContract.Presenter.State, MainRouter>(router),
    MapFragmentContract.Presenter {

    companion object {
        private const val TAG = "MapFragmentPresenter"
        private const val TASK_READ_TRACKS = "taskReadTracks"
        private const val FLAG_LOCATION = 0x0001
        private const val FLAG_TRACK = 0x0002
        private const val FLAG_SETUP_ALL = FLAG_LOCATION or FLAG_TRACK
    }

    private val log = logFactory.get(TAG)
    private val taskReadTracks = createReadTracksTask()

    private var currentLocation: Location? = null
    private var connectedLocationTracker: LocationTracker? = null
    private var locationsReceivedCount = 0
    private var latestTrack: Track? = null
    private var wasCameraMoved: Boolean = false

    private val trackingListener = object : LocationTracker.Listener {
        override fun onLocationManagerConnected() {
            updateUi(0)
        }

        override fun onLocationMangerConnectionSuspended(reason: Int) {

        }

        override fun onLocationMangerConnectionFailed(error: Throwable) {

        }

        override fun onLocationUpdated(location: Location) {
            this@MapFragmentPresenter.currentLocation = location
            val count = locationsReceivedCount++
            when {
                0 == count % 10 && true == connectedLocationTracker?.isRecording() -> {
                    if (!taskReadTracks.isRunning()) {
                        taskReadTracks.start(ReadTracksInteractor.Param(LatestTrackQuerySpecification()), Unit)
                    }
                }

                0 == count % 5 -> {
                    updateUi(0)
                }
            }
            ui.showIndicatorProgress(count.toIndicatorText())
        }

        override fun onStatusUpdated(status: LocationTracker.Status) {

        }

        override fun onLocationAvailable(isAvailable: Boolean) {

        }
    }

    override fun saveState(state: MapFragmentContract.Presenter.State) {
        super.saveState(state)
        currentLocation?.let {
            state.location = it
        }
        state.locationsReceivedCount = locationsReceivedCount
    }

    override fun restoreState(savedState: MapFragmentContract.Presenter.State?) {
        super.restoreState(savedState)
        savedState?.let {
            currentLocation = it.location
            locationsReceivedCount = it.locationsReceivedCount
        }
    }

    override fun start() {
        super.start()
        taskReadTracks.start(ReadTracksInteractor.Param(LatestTrackQuerySpecification()), Unit)
    }

    override fun stop() {
        super.stop()
        taskReadTracks.stop()
    }

    override fun locationTrackerServiceConnected(locationTracker: LocationTracker) {
        connectedLocationTracker = locationTracker
        connectedLocationTracker?.addListener(trackingListener)
        connectedLocationTracker?.connect()
        updateUi(0)
    }

    override fun locationTrackerServiceDisconnected() {
        connectedLocationTracker?.disconnect()
        connectedLocationTracker?.removeListener(trackingListener)
        connectedLocationTracker = null
        updateUi(0)
    }

    override fun mapReady() {
        updateUi(0)
    }

    override fun refreshClicked() {
        wasCameraMoved = false
        updateUi(0)
    }

    private fun updateUi(flag: Int) {
        val track = latestTrack
        val isRecording = connectedLocationTracker?.isRecording() ?: false
        val location = currentLocation

        when {
            isRecording && null != track -> {
                if (track.points.isNotEmpty()) {
                    val firstPoint = track.points.first()
                    val startLocation =
                        Location(firstPoint.latitude, firstPoint.longitude, firstPoint.altitude)

                    if (1 == track.points.size) {
                        ui.showStartMarker(null)
                        ui.showFinishMarker(startLocation, isRecording = true, shouldMoveCamera = !wasCameraMoved)
                    } else {
                        val lastPoint = track.points.last()
                        val lastLocation =
                            Location(lastPoint.latitude, lastPoint.longitude, lastPoint.altitude)
                        ui.showStartMarker(startLocation)
                        ui.showFinishMarker(lastLocation, isRecording = true, shouldMoveCamera = false)
                        ui.showTrack(track, isRecording = true, shouldMoveCamera = !wasCameraMoved)
                    }

                    if (!wasCameraMoved) {
                        wasCameraMoved = true
                    }
                } else {
                    ui.showStartMarker(null)
                    ui.showFinishMarker(null)
                    ui.showTrack(null)
                }
            }

            null != track -> {
                if (track.points.isNotEmpty()) {
                    val firstPoint = track.points.first()
                    val startLocation =
                        Location(firstPoint.latitude, firstPoint.longitude, firstPoint.altitude)
                    if (1 == track.points.size) {
                        ui.showStartMarker(null)
                        ui.showFinishMarker(startLocation, isRecording = false, shouldMoveCamera = !wasCameraMoved)
                    } else {
                        val lastPoint = track.points.last()
                        val lastLocation =
                            Location(lastPoint.latitude, lastPoint.longitude, lastPoint.altitude)
                        ui.showStartMarker(startLocation)
                        ui.showFinishMarker(lastLocation, isRecording = false, shouldMoveCamera = false)
                        ui.showTrack(track, isRecording = false, shouldMoveCamera = !wasCameraMoved)
                    }
                    if (!wasCameraMoved) {
                        wasCameraMoved = true
                    }
                } else {
                    ui.showStartMarker(null)
                    ui.showFinishMarker(null)
                    ui.showTrack(null)
                }
            }

            null != location -> {
                ui.showStartMarker(null)
                ui.showFinishMarker(location, isRecording = false, shouldMoveCamera = !wasCameraMoved)
                ui.showTrack(null)
                if (!wasCameraMoved) {
                    wasCameraMoved = true
                }
            }

            else -> {
                // do nothing
            }
        }
    }

    private fun handleReadTracks(tracks: List<Track>?, error: Throwable?) {
        if (null != tracks) {
            val latestTrack = if (tracks.isNotEmpty()) tracks[0] else null
            this.latestTrack = latestTrack

            if (null == currentLocation && null != latestTrack && latestTrack.points.isNotEmpty()) {
                val lastPoint = latestTrack.points.last()
                currentLocation =
                    Location(lastPoint.latitude, lastPoint.longitude, lastPoint.altitude)
            }
        } else if (null != error) {
            ui.showError(error)
        }
        updateUi(0)
    }

    private fun createReadTracksTask() =
        SingleResultTask<ReadTracksInteractor.Param, ReadTracksInteractor.Result, Unit>(
            TASK_READ_TRACKS,
            { param, _ ->
                readTracksInteractor.asRxSingle(param)
                    .observeOn(AndroidSchedulers.mainThread())
            },
            { result, _ ->
                handleReadTracks(result.tracks, null)
            },
            { error, _ ->
                log.e(error, "Failed to read tracks")
                handleReadTracks(null, error)
            }
        )

}

fun Int.toIndicatorText(): String =
    when(this % 4) {
        0 -> "|"
        1 -> "/"
        2 -> "-"
        3 -> "\\"
        else -> "?"
    }