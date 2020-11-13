package com.lex.simplequest.presentation.screen.home.home

import android.util.Log
import com.lex.core.log.LogFactory
import com.lex.simplequest.data.location.repository.queries.LatestTrackQuerySpecification
import com.lex.simplequest.domain.common.connectivity.InternetConnectivityTracker
import com.lex.simplequest.domain.locationmanager.LocationTracker
import com.lex.simplequest.domain.locationmanager.model.Location
import com.lex.simplequest.domain.model.Point
import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.domain.model.distance
import com.lex.simplequest.domain.track.interactor.ReadTracksInteractor
import com.lex.simplequest.presentation.base.BaseMvpLcePresenter
import com.lex.simplequest.presentation.screen.home.MainRouter
import com.lex.simplequest.presentation.utils.asRxSingle
import com.lex.simplequest.presentation.utils.tasks.MultiResultTask
import com.lex.simplequest.presentation.utils.tasks.SingleResultTask
import com.lex.simplequest.presentation.utils.toStringDurations
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class HomeFragmentPresenter(
    private val readTracksInteractor: ReadTracksInteractor,
    internetConnectivityTracker: InternetConnectivityTracker,
    logFactory: LogFactory,
    router: MainRouter
) : BaseMvpLcePresenter<HomeFragmentContract.Ui, HomeFragmentContract.Presenter.State, MainRouter>(
    internetConnectivityTracker, router
), HomeFragmentContract.Presenter {

    companion object {
        private const val TAG = "HomeFragmentPresenter"
        private const val TASK_READ_TRACKS = "taskReadTracks"
        private const val TASK_TIMER = "taskTimer"

        private const val FLAG_SET_TRACK_INFO = 0x0001
        private const val FLAG_SET_BUTTON_STATUS = 0x0002
        private const val FLAG_SET_LOCATION_AVAILABILITY_STATUS = 0x0004
        private const val FLAG_SET_LOCATION_SUSPENDED_STATUS = 0x0008
        private const val FLAG_SET_ERROR_STATUS = 0x0010
        private const val FLAG_START_TIMER = 0x0020
        private const val FLAG_SETUP_UI =
            FLAG_SET_TRACK_INFO or FLAG_SET_BUTTON_STATUS or FLAG_SET_LOCATION_AVAILABILITY_STATUS or
                    FLAG_SET_LOCATION_SUSPENDED_STATUS or FLAG_SET_ERROR_STATUS
        private const val METERS_IN_KILOMETER = 1000.0f
    }

    private val log = logFactory.get(TAG)
    private val taskReadTracks = createReadTracksTask()
    private val taskTimer = createTimerTask()

    private var connectedLocationTracker: LocationTracker? = null
    private var lastTrack: Track? = null
    private var isLocationAvailable: Boolean? = null
    private var locationSuspendedReason: Int? = null
    private val newRecordedLocations = mutableListOf<Location>()

    private val trackingListener = object : LocationTracker.Listener {

        override fun onLocationManagerConnected() {
            Log.d("qaz", "1.onLocationManger Connected")
            updateUi(FLAG_SETUP_UI)
        }

        override fun onLocationMangerConnectionSuspended(reason: Int) {
            Log.d("qaz", "2.onLocationManger Suspended")
            locationSuspendedReason = reason
            updateUi(FLAG_SET_LOCATION_SUSPENDED_STATUS)
        }

        override fun onLocationMangerConnectionFailed(error: Throwable) {
            Log.d("qaz", "3.onLocationManger Failed")
            this@HomeFragmentPresenter.error = error
            updateUi(FLAG_SET_ERROR_STATUS)
        }

        override fun onLocationUpdated(location: Location) {
            Log.d("qaz", "4.onLocationManger Updated")
            if (true == connectedLocationTracker?.isRecording()) {
                newRecordedLocations.add(location)
            }
            updateUi(FLAG_SET_TRACK_INFO or FLAG_SET_BUTTON_STATUS)
        }

        override fun onStatusUpdated(status: LocationTracker.Status) {
            Log.d("qaz", "5.onStatus Updated: $status")
            if (status == LocationTracker.Status.RECORDING) {
                updateUi(FLAG_SETUP_UI or FLAG_START_TIMER)
            } else {
                updateUi(FLAG_SETUP_UI)
            }
        }

        override fun onLocationAvailable(isAvailable: Boolean) {
            Log.d("qaz", "onLocationAvailable")
            isLocationAvailable = isAvailable
            updateUi(FLAG_SET_LOCATION_AVAILABILITY_STATUS)
        }
    }

    override fun saveState(state: HomeFragmentContract.Presenter.State) {
        super.saveState(state)
        state.error = error
        state.isLocationAvailable = isLocationAvailable
        state.locationSuspendedReason = locationSuspendedReason
    }

    override fun restoreState(savedState: HomeFragmentContract.Presenter.State?) {
        super.restoreState(savedState)

        savedState?.let { state ->
            error = state.error
            isLocationAvailable = state.isLocationAvailable
            locationSuspendedReason = state.locationSuspendedReason
        }
    }

    override fun start() {
        super.start()
        taskReadTracks.start(ReadTracksInteractor.Param(LatestTrackQuerySpecification()), Unit)
        updateUi(FLAG_SETUP_UI)
    }

    override fun stop() {
        super.stop()
        taskReadTracks.stop()
        taskTimer.stop()
    }

    override fun startStopClicked() {
        connectedLocationTracker?.let { tracker ->
            if (tracker.isRecording()) {
                Log.d("qaz", "STOP recording")
                taskTimer.stop()
                tracker.stopRecording()
            } else {
                Log.e("qaz", "START recording")
                tracker.startRecording()
                if (taskReadTracks.isRunning()) {
                    taskReadTracks.stop()
                }
                taskReadTracks.start(
                    ReadTracksInteractor.Param(LatestTrackQuerySpecification()),
                    Unit
                )
            }
        }
        updateUi(FLAG_SET_BUTTON_STATUS)
    }

    override fun locationTrackerServiceConnected(locationTracker: LocationTracker) {
        connectedLocationTracker = locationTracker
        connectedLocationTracker?.addListener(trackingListener)
        Log.i("qaz", "location tracker connected in presenter")

        updateUi(FLAG_SET_TRACK_INFO or FLAG_SET_BUTTON_STATUS)
    }

    override fun locationTrackerServiceDisconnected() {
        connectedLocationTracker?.removeListener(trackingListener)
        connectedLocationTracker = null
        Log.i("qaz", "location tracker disconnected in presenter")
        taskTimer.stop()
        updateUi(FLAG_SET_TRACK_INFO or FLAG_SET_BUTTON_STATUS)
    }

    override fun isLoading(): Boolean =
        false

    override fun reload() {

    }

    fun updateUi(flags: Int) {
        Log.d(
            "qaz",
            "updateUi, connectedLocationTracker, recording: ${connectedLocationTracker?.isRecording()} flags: $flags"
        )
        super.updateUi()

        val tracker = connectedLocationTracker
        if (null != tracker) {
            ui.showProgress(false)

            if (0 != (flags and FLAG_SET_TRACK_INFO)) {
                val track = lastTrack
                val shouldStartTimer = (0 != (flags and FLAG_START_TIMER))
                if (tracker.isRecording() && !taskTimer.isRunning() && track != null && shouldStartTimer) {
                    taskTimer.start(track.startTime, Unit)
                } else if (!tracker.isRecording() && taskTimer.isRunning()) {
                    taskTimer.stop()
                }

                ui.showLastTrackName(track?.name, tracker.isRecording())

                if (null != track) {
                    val endTime = track.endTime ?: System.currentTimeMillis()
                    val durationInSeconds = endTime - track.startTime
                    val (minutes, seconds) = durationInSeconds.toStringDurations()
                    ui.showLastTrackDuration(minutes, seconds)
                    val originDistance = track.distance()
                    val originLocation = if (track.points.isNotEmpty()) Location(
                        track.points[0].latitude,
                        track.points[0].longitude,
                        track.points[0].altitude
                    ) else null
                    val additionalDistance = newRecordedLocations.additionalDistance(originLocation)
                    val format: String
                    var summaryDistance = originDistance + additionalDistance
                    val withBoldStyle: Boolean
                    if (summaryDistance >= METERS_IN_KILOMETER) {
                        format = "%.2f km"
                        summaryDistance /= METERS_IN_KILOMETER
                        withBoldStyle = true
                    } else {
                        format = "%.2f m"
                        withBoldStyle = false
                    }

                    ui.showLastTrackDistance(String.format(format, summaryDistance), withBoldStyle)
                }
            }

            if (0 != (flags and FLAG_SET_BUTTON_STATUS)) {
                val status: RecordButtonType =
                    when {
                        tracker.isRecording() -> {
                            RecordButtonType.RECORDING
                        }
                        tracker.isConnecting() -> {
                            RecordButtonType.GOING_TO_RECORD
                        }
                        else -> RecordButtonType.STOPPED
                    }

                ui.setButtonStyleRecording(status)
            }
            ui.setTrackerStatus(tracker.getStatus())
        } else {
            if (0 != (flags and FLAG_SET_BUTTON_STATUS)) {
                ui.setButtonStyleRecording(RecordButtonType.STOPPED)
            }
            ui.setTrackerStatus(null)
        }

        if (0 != (flags and FLAG_SET_LOCATION_AVAILABILITY_STATUS)) {
            ui.setLocationAvailableStatus(isLocationAvailable)
        }

        if (0 != (flags and FLAG_SET_LOCATION_SUSPENDED_STATUS)) {
            ui.setLocationSuspendedStatus(locationSuspendedReason)
        }

        if (0 != (flags and FLAG_SET_ERROR_STATUS)) {
            ui.setError(error)
        }
    }

    private fun handleReadTracks(tracks: List<Track>?, error: Throwable?) {
        if (null != tracks) {
            val lastTrack = if (tracks.isNotEmpty()) tracks.last() else null
            this.lastTrack = lastTrack
            newRecordedLocations.clear()
        } else if (null != error) {
            this.error = error
            updateUi(FLAG_SET_ERROR_STATUS)
        }

        val tracker = connectedLocationTracker
        val track = lastTrack
        if (tracker != null && track != null) {
            if (!tracker.isRecording() && taskTimer.isRunning()) {
                taskTimer.stop()
            }
        }

        updateUi(FLAG_SET_TRACK_INFO or FLAG_SET_BUTTON_STATUS or FLAG_START_TIMER)
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

    private fun updateTimer(timer: Long) {
        val (minutes, seconds) = timer.toStringDurations()
        ui.showLastTrackDuration(minutes, seconds)
    }

    private fun createTimerTask() =
        MultiResultTask<Long, Long, Unit>(
            TASK_TIMER,
            { origin, _ ->
                Observable.interval(500, TimeUnit.MILLISECONDS)
                    .map {
                        System.currentTimeMillis() - origin
                    }
                    .observeOn(AndroidSchedulers.mainThread())
            },
            { timer, _ ->
                updateTimer(timer)
            },
            { error, _ ->
                log.e(error, "Timer failed")
            }
        )
}

// Optimize
fun List<Location>.additionalDistance(origin: Location?): Float {
    if (this.isEmpty()) {
        return .0f
    }

    if (null == origin && this.size < 2) {
        return .0f
    }

    val results = FloatArray(3)
    var distanceInMeters = .0f

    if (null != origin) {
        android.location.Location.distanceBetween(
            origin.latitude,
            origin.longitude,
            this[0].latitude,
            this[0].longitude,
            results
        )
        distanceInMeters = results[0]
    }
    for (i in 1 until this.size) {
        val start = this[i - 1]
        val end = this[i]
        android.location.Location.distanceBetween(
            start.latitude,
            start.longitude,
            end.latitude,
            end.longitude,
            results
        )
        distanceInMeters += results[0]
    }

    return distanceInMeters
}