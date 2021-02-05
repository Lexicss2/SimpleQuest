package com.lex.simplequest.presentation.screen.home.home

import com.lex.core.log.LogFactory
import com.lex.simplequest.BuildConfig
import com.lex.simplequest.Config
import com.lex.simplequest.data.location.repository.queries.LatestTrackQuerySpecification
import com.lex.simplequest.data.location.repository.queries.TrackByIdQuerySpecification
import com.lex.simplequest.domain.common.connectivity.InternetConnectivityTracker
import com.lex.simplequest.domain.locationmanager.LocationTracker
import com.lex.simplequest.domain.locationmanager.model.Location
import com.lex.simplequest.domain.model.*
import com.lex.simplequest.domain.permission.repository.PermissionChecker
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
    private val permissionChecker: PermissionChecker,
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
        private const val FLAG_SET_SPEED = 0x0020
        private const val FLAG_SETUP_UI =
            FLAG_SET_TRACK_INFO or FLAG_SET_DISTANCE or FLAG_SET_DURATION or FLAG_SET_BUTTON_STATUS or
                    FLAG_SET_LOCATION_ERROR_DATA or FLAG_SET_SPEED

        private val LOCATION_PERMISSIONS_SET = setOf(PermissionChecker.Permission.ACCESS_COARSE_LOCATION,
        PermissionChecker.Permission.ACCESS_FINE_LOCATION)
    }

    private val log = logFactory.get(TAG)
    private val taskReadTracks = createReadTracksTask()
    private val taskTimer = createTimerTask()

    private var connectedLocationTracker: LocationTracker? = null
    private var lastTrack: Track? = null
    private var isLocationAvailable: Boolean? = null
    private var locationSuspendedReason: Int? = null
    private val newRecordedLocations = mutableListOf<Pair<Long, Location>>()
    private var timerValue: Long? = null
    private var isRecordingRequested: Boolean = false

    private val trackingListener = object : LocationTracker.Listener {

        override fun onLocationManagerConnected() {
            log.d("1.onLocationManger Connected")
            updateUi(FLAG_SET_LOCATION_ERROR_DATA)
        }

        override fun onLocationMangerConnectionSuspended(reason: Int) {
            log.d("2.onLocationManger Suspended")
            locationSuspendedReason = reason
            updateUi(FLAG_SET_LOCATION_ERROR_DATA)
        }

        override fun onLocationMangerConnectionFailed(error: Throwable) {
            log.d("3.onLocationManger Failed")
            this@HomeFragmentPresenter.error = error
            updateUi(FLAG_SET_LOCATION_ERROR_DATA)
        }

        override fun onLocationUpdated(location: Location) {
            log.d("4.onLocationManger Updated")
            if (true == connectedLocationTracker?.isRecording()) {
                newRecordedLocations.add(Pair(System.currentTimeMillis(), location))
            }
            updateUi(FLAG_SET_DISTANCE or FLAG_SET_SPEED)
        }

        override fun onStatusUpdated(status: LocationTracker.Status) {
            log.d("5.onStatus Updated: $status")
            updateUi(FLAG_SET_TRACK_INFO or FLAG_SET_BUTTON_STATUS)
        }

        override fun onLocationAvailable(isAvailable: Boolean) {
            log.d("6. onLocationAvailable")
            isLocationAvailable = isAvailable
            updateUi(FLAG_SET_LOCATION_ERROR_DATA)
        }
    }

    private val startStopRecordResultListener = object : LocationTracker.RecordingEventsListener {
        override fun onRecordStartSucceeded(trackId: Long) {
            log.i("I record started, trackId: $trackId")
            if (taskReadTracks.isRunning()) {
                taskReadTracks.stop()
            }
            taskReadTracks.start(
                ReadTracksInteractor.Param(TrackByIdQuerySpecification(trackId)),
                Unit
            )
        }

        override fun onRecordStartFailed(error: Throwable) {
            log.e("II record start failed: ${error.localizedMessage}")
            this@HomeFragmentPresenter.error = error
            updateUi(FLAG_SET_LOCATION_ERROR_DATA)
        }

        override fun onRecordStopSucceeded(success: Boolean) {
            log.d("III record stopped Succedded: $success")
            if (taskReadTracks.isRunning()) {
                taskReadTracks.stop()
            }
            taskReadTracks.start(ReadTracksInteractor.Param(LatestTrackQuerySpecification()), Unit)
        }

        override fun onRecordStopFailed(error: Throwable) {
            log.e("IV Failed to stop record: ${error.localizedMessage}")
        }

        override fun onPauseResumeSucceeded(succeeded: Boolean) {
            log.d("V record Paused or Resumed")
            if (!taskReadTracks.isRunning()) {
                taskReadTracks.start(
                    ReadTracksInteractor.Param(LatestTrackQuerySpecification()),
                    Unit
                )
            }
        }

        override fun onPauseResumeFailed(error: Throwable) {
            log.e( "VI record Paused or Resumed failed: $error")
        }
    }

    override fun saveState(state: HomeFragmentContract.Presenter.State) {
        super.saveState(state)
        state.error = error
        state.isLocationAvailable = isLocationAvailable
        state.locationSuspendedReason = locationSuspendedReason
        state.isRecordingRequested = isRecordingRequested
    }

    override fun restoreState(savedState: HomeFragmentContract.Presenter.State?) {
        super.restoreState(savedState)

        savedState?.let { state ->
            error = state.error
            isLocationAvailable = state.isLocationAvailable
            locationSuspendedReason = state.locationSuspendedReason
            state.isRecordingRequested = isRecordingRequested
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
                log.d("STOP recording")
                tracker.stopRecording()
            } else {
                if (permissionChecker.checkAllPermissionGranted(LOCATION_PERMISSIONS_SET)) {
                    startRecording()
                } else {
                    ui.requestPermissions(LOCATION_PERMISSIONS_SET)
                }
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
        log.d("location tracker connected in presenter")
        if (isRecordingRequested) {
            isRecordingRequested = false
            startRecording()
        }

        updateUi(FLAG_SET_TRACK_INFO or FLAG_SET_BUTTON_STATUS or FLAG_SET_LOCATION_ERROR_DATA)
    }

    override fun locationTrackerServiceDisconnected() {
        connectedLocationTracker?.removeListener(trackingListener)
        connectedLocationTracker?.recordingEventsListener = null
        connectedLocationTracker = null
        log.d("location tracker disconnected in presenter")
        updateUi(FLAG_SET_LOCATION_ERROR_DATA)
    }

    override fun permissionsGranted() {
        isRecordingRequested = true
    }

    override fun permissionsDenied() {
        ui.showLocationPermissionRationale()
    }

    override fun isLoading(): Boolean =
        false

    override fun reload() {

    }

    private fun startRecording() {
        connectedLocationTracker?.let { tracker ->
            log.d("START recording")
            tracker.startRecording()
            lastTrack = null
        }
    }


    private fun handleTimerAction(tracker: LocationTracker?, track: Track?) {
        if (null != tracker) {
            val isRecordingNow = tracker.isRecording() && !tracker.isRecordingPaused()
            log.v(
                "isRecNow = $isRecordingNow, track assigned = ${null != track}, timer running = ${taskTimer.isRunning()}"
            )
            if (isRecordingNow && null != track && !taskTimer.isRunning()) {
                val started =
                    taskTimer.start(track.startTime + track.pausedDuration(), Unit) // TODO: Fix it
                log.w( "Timer started $started, isRunning = ${taskTimer.isRunning()}")
            } else if (!isRecordingNow && taskTimer.isRunning()) {
                val stopped = taskTimer.stop()
                log.i( "Timer stopped: $stopped")
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
                } else {
                    ui.showLastTrackDistance(null, false)
                }
            }

            if (0 != (flags and FLAG_SET_SPEED)) {
                if (null != track) {
                    val showCurrent = tracker?.getRecordingStatus() == RecordingStatus.RECORDING
                    val speed = if (showCurrent) newRecordedLocations.currentSpeed(track) else track.averageSpeed()
                    ui.showSpeed(String.format("%.2f", speed), isCurrent = showCurrent)
                } else {
                    ui.showSpeed(null, false)
                }
            }

            if (0 != (flags and FLAG_SET_DURATION)) {
                if (null != track) {
                    val isNowRecording =
                        null != tracker && tracker.isRecording() && !tracker.isRecordingPaused()

                    //val isPaused = tracker?.isRecordingPaused() ?: false
                    val timeDuration = if (null != timerValue) timerValue!!.toStringDurations() else null
                    val movingDuration = track.movingDuration(isNowRecording).toStringDurations()

                    log.v( "timerValue = $timeDuration, trackValue = $movingDuration")

                    val duration =
                        if (isNowRecording) timerValue!! else track.movingDuration(isNowRecording)
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

                if (BuildConfig.DEBUG && Config.SHOW_DEBUG_INFO) {
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
            log.w("Update by handleReadTracks = and track Moving duration is =" +
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

        updateUi(FLAG_SET_TRACK_INFO or FLAG_SET_DISTANCE or FLAG_SET_DURATION or FLAG_SET_SPEED)
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
                log.d( "run timer observable")
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
fun List<Pair<Long, Location>>.additionalDistance(origin: Location?): Float {
    if (this.isEmpty()) {
        return .0f
    }

    if (null == origin && this.size < 2) {
        return .0f
    }

    val results = FloatArray(3)
    var distanceInMeters = .0f

    if (null != origin) {
        val (_, l) = this.first()
        android.location.Location.distanceBetween(
            origin.latitude,
            origin.longitude,
            l.latitude,
            l.longitude,
            results
        )
        distanceInMeters = results[0]
    }
    for (i in 1 until this.size) {
        val (_, startLocation) = this[i - 1]
        val (_, endLocation) = this[i]
        android.location.Location.distanceBetween(
            startLocation.latitude,
            startLocation.longitude,
            endLocation.latitude,
            endLocation.longitude,
            results
        )
        distanceInMeters += results[0]
    }

    return distanceInMeters
}

fun List<Pair<Long, Location>>.currentSpeed(track: Track?): Float {
    val allLocations = (track?.points?.toTimedLocations() ?: emptyList()) + this
    return when(allLocations.size) {
        0 -> .0f
        1 -> .0f
        else -> {
            val results = FloatArray(3)
            val (startTime, startLocation) = allLocations[allLocations.lastIndex - 1]
            val (endTime, endLocation) = allLocations.last()
            android.location.Location.distanceBetween(
                startLocation.latitude,
                startLocation.longitude,
                endLocation.latitude,
                endLocation.longitude,
                results
            )
            val d = results[0]
            val t = (endTime - startTime).toFloat()
            (d / Config.METERS_IN_KILOMETER) / (t / (1000.0f * 60.0f * 60.0f))
        }
    }
}