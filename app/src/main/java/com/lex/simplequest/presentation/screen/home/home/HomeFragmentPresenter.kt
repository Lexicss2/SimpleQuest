package com.lex.simplequest.presentation.screen.home.home

import android.util.Log
import com.lex.core.log.LogFactory
import com.lex.simplequest.BuildConfig
import com.lex.simplequest.Config
import com.lex.simplequest.data.location.repository.queries.LatestTrackQuerySpecification
import com.lex.simplequest.data.location.repository.queries.TrackByIdQuerySpecification
import com.lex.simplequest.domain.common.connectivity.InternetConnectivityTracker
import com.lex.simplequest.domain.locationmanager.LocationTracker
import com.lex.simplequest.domain.locationmanager.model.Location
import com.lex.simplequest.domain.model.*
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
        private const val FLAG_SET_DISTANCE = 0x0002
        private const val FLAG_SET_DURATION = 0x0004
        private const val FLAG_SET_BUTTON_STATUS = 0x0008
        private const val FLAG_SET_LOCATION_ERROR_DATA = 0x0010
        private const val FLAG_SETUP_UI =
            FLAG_SET_TRACK_INFO or FLAG_SET_DISTANCE or FLAG_SET_DURATION or FLAG_SET_BUTTON_STATUS or FLAG_SET_LOCATION_ERROR_DATA
    }

    private val log = logFactory.get(TAG)
    private val taskReadTracks = createReadTracksTask()
    private val taskTimer = createTimerTask()

    private var connectedLocationTracker: LocationTracker? = null
    private var lastTrack: Track? = null
    private var isLocationAvailable: Boolean? = null
    private var locationSuspendedReason: Int? = null
    private val newRecordedLocations = mutableListOf<Location>()
    private var timerValue: Long? = null

    private val trackingListener = object : LocationTracker.Listener {

        override fun onLocationManagerConnected() {
            Log.d(TAG, "1.onLocationManger Connected")
            updateUi(FLAG_SET_LOCATION_ERROR_DATA)
        }

        override fun onLocationMangerConnectionSuspended(reason: Int) {
            Log.d(TAG, "2.onLocationManger Suspended")
            locationSuspendedReason = reason
            updateUi(FLAG_SET_LOCATION_ERROR_DATA)
        }

        override fun onLocationMangerConnectionFailed(error: Throwable) {
            Log.d(TAG, "3.onLocationManger Failed")
            this@HomeFragmentPresenter.error = error
            updateUi(FLAG_SET_LOCATION_ERROR_DATA)
        }

        override fun onLocationUpdated(location: Location) {
            Log.d(TAG, "4.onLocationManger Updated")
            if (true == connectedLocationTracker?.isRecording()) {
                newRecordedLocations.add(location)
            }
            updateUi(FLAG_SET_DISTANCE)
        }

        override fun onStatusUpdated(status: LocationTracker.Status) {
            Log.d(TAG, "5.onStatus Updated: $status")
            updateUi(FLAG_SET_TRACK_INFO or FLAG_SET_BUTTON_STATUS)
        }

        override fun onLocationAvailable(isAvailable: Boolean) {
            Log.d(TAG, "6. onLocationAvailable")
            isLocationAvailable = isAvailable
            updateUi(FLAG_SET_LOCATION_ERROR_DATA)
        }
    }

    private val startStopRecordResultListener = object : LocationTracker.RecordingEventsListener {
        override fun onRecordStartSucceeded(trackId: Long) {
            Log.i(TAG, "I record started, trackId: $trackId")
            if (taskReadTracks.isRunning()) {
                taskReadTracks.stop()
            }
            taskReadTracks.start(
                ReadTracksInteractor.Param(TrackByIdQuerySpecification(trackId)),
                Unit
            )
        }

        override fun onRecordStartFailed(error: Throwable) {
            Log.e(TAG, "II record start failed: ${error.localizedMessage}")
            this@HomeFragmentPresenter.error = error
            updateUi(FLAG_SET_LOCATION_ERROR_DATA)
        }

        override fun onRecordStopSucceeded(success: Boolean) {
            Log.d(TAG, "III record stopped Succedded: $success")
            if (taskReadTracks.isRunning()) {
                taskReadTracks.stop()
            }
            taskReadTracks.start(ReadTracksInteractor.Param(LatestTrackQuerySpecification()), Unit)
        }

        override fun onRecordStopFailed(error: Throwable) {
            Log.e(TAG, "IV Failed to stop record: ${error.localizedMessage}")
        }

        override fun onPauseResumeSucceeded(succeeded: Boolean) {
            Log.d(TAG, "V record Paused or Resumed")
            if (!taskReadTracks.isRunning()) {
                taskReadTracks.start(
                    ReadTracksInteractor.Param(LatestTrackQuerySpecification()),
                    Unit
                )
            }
        }

        override fun onPauseResumeFailed(error: Throwable) {
            Log.e(TAG, "VI record Paused or Resumed failed: $error")
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
                Log.d(TAG, "STOP recording")
                tracker.stopRecording()
            } else {
                Log.e(TAG, "START recording")
                tracker.startRecording()
                lastTrack = null
            }
        }
        updateUi(FLAG_SET_BUTTON_STATUS)
    }

    override fun pauseResumeClicked() {
        connectedLocationTracker?.pauseOrResume()
        updateUi(FLAG_SET_BUTTON_STATUS)
    }

    override fun locationTrackerServiceConnected(locationTracker: LocationTracker) {
        connectedLocationTracker = locationTracker
        connectedLocationTracker?.addListener(trackingListener)
        connectedLocationTracker?.recordingEventsListener = startStopRecordResultListener
        Log.d(TAG, "location tracker connected in presenter")

        updateUi(FLAG_SET_TRACK_INFO or FLAG_SET_BUTTON_STATUS or FLAG_SET_LOCATION_ERROR_DATA)
    }

    override fun locationTrackerServiceDisconnected() {
        connectedLocationTracker?.removeListener(trackingListener)
        connectedLocationTracker?.recordingEventsListener = null
        connectedLocationTracker = null
        Log.d(TAG, "location tracker disconnected in presenter")
        updateUi(FLAG_SET_LOCATION_ERROR_DATA)
    }

    override fun isLoading(): Boolean =
        false

    override fun reload() {

    }

    private fun handleTimerAction(tracker: LocationTracker?, track: Track?) {
        if (null != tracker) {
            val isRecordingNow = tracker.isRecording() && !tracker.isRecordingPaused()
            Log.v(
                TAG,
                "isRecNow = $isRecordingNow, track assigned = ${null != track}, timer running = ${taskTimer.isRunning()}"
            )
            if (isRecordingNow && null != track && !taskTimer.isRunning()) {
                val started =
                    taskTimer.start(track.startTime + track.pausedDuration(), Unit) // TODO: Fix it
                Log.w(TAG, "Timer started $started, isRunning = ${taskTimer.isRunning()}")
            } else if (!isRecordingNow && taskTimer.isRunning()) {
                val stopped = taskTimer.stop()
                Log.i(TAG, "Timer stopped: $stopped")
            }
        }
    }

    private fun updateUi(flags: Int) {
        super.updateUi()

        if (isUiBinded) {
            val tracker = connectedLocationTracker
            val track = lastTrack
            ui.showProgress(taskReadTracks.isRunning())
            handleTimerAction(tracker, track)


            if (0 != (flags and FLAG_SET_TRACK_INFO)) {
                val recordingStatus = tracker?.getRecordingStatus()
                ui.showLastTrackName(track?.name, recordingStatus)
                // show speed when needed
            }

            if (0 != (flags and FLAG_SET_DISTANCE)) {
                if (null != track) {
                    val originDistance = track.movingDistance() // Already recorded distance
                    val lastLocation = if (track.points.isNotEmpty()) {
                        val last = track.points.last()
                        Location(
                            last.latitude,
                            last.longitude,
                            last.altitude
                        )
                    } else null
                    val additionalDistance =
                        newRecordedLocations.additionalDistance(lastLocation) // Not yet recorded distance
                    val format: String
                    var summaryDistance = originDistance + additionalDistance
                    val withBoldStyle: Boolean
                    if (summaryDistance >= Config.METERS_IN_KILOMETER) {
                        format = "%.2f"
                        summaryDistance /= Config.METERS_IN_KILOMETER
                        withBoldStyle = true
                    } else {
                        format = "%.2f"
                        withBoldStyle = false
                    }
                    ui.showLastTrackDistance(String.format(format, summaryDistance), withBoldStyle)

                    val showCurrent = tracker?.getRecordingStatus() == RecordingStatus.RECORDING
                    val speed = if (showCurrent) track.currentSpeed() else track.averageSpeed()
                    ui.showSpeed(String.format("%.2f", speed), isCurrent = showCurrent)
                } else {
                    ui.showLastTrackDistance(null, false)
                    ui.showSpeed(null, false)
                }
            }

            if (0 != (flags and FLAG_SET_DURATION)) {
                if (null != track) {
                    val isNowRecording =
                        null != tracker && tracker.isRecording() && !tracker.isRecordingPaused()

                    val isPaused = tracker?.isRecordingPaused() ?: false
                    val dur_tm = if (null != timerValue) timerValue!!.toStringDurations() else null
                    val dur_tr = track.movingDuration(!isPaused).toStringDurations()

                    Log.v(TAG, "timerValue = ${dur_tm}, trackValue = ${dur_tr}")

                    val duration = if (isNowRecording) timerValue!! else track.movingDuration(!isPaused)
                    val (minutes, seconds) = duration.toStringDurations()
                    ui.showLastTrackDuration(minutes, seconds)
                } else {
                    ui.showLastTrackDuration(null, null)
                }
            }

            if (0 != (flags and FLAG_SET_BUTTON_STATUS)) {
                if (null != tracker) {
                    val status: RecordingStatus = tracker.getRecordingStatus()
                    ui.setButtonStyleRecording(status)
                } else {
                    ui.setButtonStyleRecording(null)
                }

                if (BuildConfig.DEBUG) {
                    ui.setTrackerStatus(
                        tracker?.getStatus(),
                        "point, newRecorded = ${newRecordedLocations.size}"
                    )
                }
            }

            if (0 != (flags and FLAG_SET_LOCATION_ERROR_DATA)) {
                ui.setLocationAvailableStatus(isLocationAvailable)
                ui.setLocationSuspendedStatus(locationSuspendedReason)
                ui.setError(error)
            }
        }
    }

    private fun handleReadTracks(tracks: List<Track>?, error: Throwable?) {
        if (null != tracks) {
            val lastTrack = if (tracks.isNotEmpty()) tracks.last() else null
            this.lastTrack = lastTrack
            newRecordedLocations.clear()
            val isPaused = connectedLocationTracker?.isRecordingPaused() ?: false
            Log.w(
                TAG, "Update by handleReadTracks = and track Moving duration is =" +
                        " ${
                            lastTrack?.movingDuration(!isPaused)?.toStringDurations()
                        }, full = ${lastTrack?.fullDuration(!isPaused)?.toStringDurations()}"
            )
        } else if (null != error) {
            this.error = error
            updateUi(FLAG_SET_LOCATION_ERROR_DATA)
        }

        val tracker = connectedLocationTracker
        val track = lastTrack
        if (tracker != null && track != null) {
            if (!tracker.isRecording() && taskTimer.isRunning()) {
                taskTimer.stop()
            }
        }

        updateUi(FLAG_SET_TRACK_INFO or FLAG_SET_DISTANCE or FLAG_SET_DURATION)
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
        timerValue = timer
        updateUi(FLAG_SET_DURATION)
    }

    private fun createTimerTask() =
        MultiResultTask<Long, Long, Unit>(
            TASK_TIMER,
            { origin, _ ->
                Log.d(TAG, "run timer observable")
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
                Log.e(TAG, "timer failed - ${error.localizedMessage}")
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