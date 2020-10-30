package com.lex.simplequest.presentation.screen.home.home

import android.util.Log
import com.lex.core.log.LogFactory
import com.lex.simplequest.data.location.repository.AllTracksSpecification
import com.lex.simplequest.domain.common.connectivity.InternetConnectivityTracker
import com.lex.simplequest.domain.locationmanager.LocationTracker
import com.lex.simplequest.domain.locationmanager.model.Location
import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.domain.track.interactor.ReadTracksInteractor
import com.lex.simplequest.presentation.base.BaseMvpLcePresenter
import com.lex.simplequest.presentation.screen.home.MainRouter
import com.lex.simplequest.presentation.screen.home.tracks.TracksFragmentPresenter
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
    }

    private val log = logFactory.get(TAG)
    private var connectedlocationTracker: LocationTracker? = null
    private val taskReadTracks = createReadTracksTask()
    private val taskTimer = createTimerTask()
    private var lastTrack: Track? = null

    private val trackingListener = object : LocationTracker.Listener {

        override fun onLocationManagerConnected() {
            Log.d("qaz", "1.onLocationManger Connected")
            updateUi()
        }

        override fun onLocationMangerConnectionSuspended(reason: Int) {
            Log.d("qaz", "2.onLocationManger Suspended")
            updateUi()
        }

        override fun onLocationMangerConnectionFailed(error: Throwable) {
            Log.d("qaz", "3.onLocationManger Failed")
            updateUi()
        }

        override fun onLocationUpdated(location: Location) {
            Log.d("qaz", "4.onLocationManger Updated")
            updateUi()
        }

        override fun onStatusUpdated(status: LocationTracker.Status) {
            Log.d("qaz", "5.onStatus Updated: $status")
            updateUi()
        }
    }

    override fun start() {
        super.start()
        taskReadTracks.start(ReadTracksInteractor.Param(AllTracksSpecification()), Unit)
        updateUi()
    }

    override fun stop() {
        super.stop()
        taskReadTracks.stop()
        taskTimer.stop()
    }

    override fun startStopClicked() {
        connectedlocationTracker?.let { tracker ->
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
                taskReadTracks.start(ReadTracksInteractor.Param(AllTracksSpecification()), Unit)
            }
        }
        updateUi()
    }

    override fun locationTrackerConnected(locationTracker: LocationTracker) {
        connectedlocationTracker = locationTracker
        connectedlocationTracker?.addListener(trackingListener)
        Log.i("qaz", "location tracker connected in presenter")

        updateUi()
    }

    override fun locationTrackerDisconnected() {
        connectedlocationTracker?.removeListener(trackingListener)
        connectedlocationTracker = null
        Log.i("qaz", "location tracker disconnected in presenter")
        taskTimer.stop()
        updateUi()
    }

    override fun isLoading(): Boolean =
        false

    override fun reload() {

    }

    override fun updateUi() {
        Log.d("qaz", "updateUi, connectedLocationTracker, recording: ${connectedlocationTracker?.isRecording()}")

        super.updateUi()
        ui.showProgress(taskReadTracks.isRunning())
        connectedlocationTracker?.let { tracker ->

            val track = lastTrack
            if (tracker.isRecording() && !taskTimer.isRunning() && track != null) {
                taskTimer.start(track.startTime, Unit)
            } else if (!tracker.isRecording() && taskTimer.isRunning()) {
                taskTimer.stop()
            }

            ui.setButtonStyleRecording(tracker.isRecording())
            ui.showLastTrackInfo(lastTrack, tracker.isRecording())

            lastTrack?.let { track ->
                val endTime = track.endTime ?: System.currentTimeMillis()
                val durationInSeconds = endTime - track.startTime
                val (minutes, seconds) = durationInSeconds.toStringDurations()
                ui.setDurationMinutesSeconds(minutes, seconds)
            }
        }
    }

    private fun handleReadTracks(tracks: List<Track>?, error: Throwable?) {
        if (null != tracks) {
            val lastTrack = if (tracks.isNotEmpty()) tracks.last() else null
            this.lastTrack = lastTrack
        } else if (null != error) {
            // Show error
        }

        val tracker = connectedlocationTracker
        val track = lastTrack
        if (tracker != null && track != null) {
            if (!tracker.isRecording() && taskTimer.isRunning()) {
                taskTimer.stop()
            }
        }

        updateUi()
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
        // time passed in minutes
        val (minutes, seconds) = timer.toStringDurations()
        ui.setDurationMinutesSeconds(minutes, seconds)
    }

    private fun createTimerTask() =
        MultiResultTask<Long, Long, Unit>(
            TASK_TIMER,
            { origin, _ ->
                Observable.interval(1000, TimeUnit.MILLISECONDS)
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