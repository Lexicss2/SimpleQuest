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
import com.lex.simplequest.presentation.utils.tasks.SingleResultTask
import io.reactivex.android.schedulers.AndroidSchedulers

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
    }

    private val log = logFactory.get(TAG)
    private var connectedlocationTracker: LocationTracker? = null
    private val taskReadTracks = createReadTracksTask()

    private val trackingListener = object : LocationTracker.Listener {

        override fun onLocationManagerConnected() {
            updateUi()
        }

        override fun onLocationMangerConnectionSuspended(reason: Int) {
            updateUi()
        }

        override fun onLocationMangerConnectionFailed(error: Throwable) {
            updateUi()
        }

        override fun onLocationUpdated(location: Location) {
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
    }

    override fun startStopClicked() {
        connectedlocationTracker?.let { tracker ->
            if (tracker.isRecording()) {
                tracker.stopRecording()
            } else {
                tracker.startRecording()
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
        updateUi()
    }

    override fun isLoading(): Boolean =
        false

    override fun reload() {

    }

    override fun updateUi() {
        super.updateUi()
        ui.showProgress(taskReadTracks.isRunning())
        connectedlocationTracker?.let { tracker ->
//            if (tracker.isRecording()) {
//                ui.setButtonCaptionAsStop()
//            } else {
//                ui.setButtonCaptionAsStart()
//            }
            ui.setButtonStyleRecording(tracker.isRecording())
        }
    }

    private fun handleReadTracks(tracks: List<Track>?, error: Throwable?) {
        if (null != tracks) {
            val lastTrack = if (tracks.isNotEmpty()) tracks.last() else null
            ui.showLastTrackInfo(lastTrack)
        } else if (null != error) {
            // Show error
        }
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