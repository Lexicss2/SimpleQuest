package com.lex.simplequest.presentation.screen.home.home

import android.util.Log
import com.lex.simplequest.domain.common.connectivity.InternetConnectivityTracker
import com.lex.simplequest.domain.locationmanager.LocationTracker
import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.presentation.base.BaseMvpLcePresenter
import com.lex.simplequest.presentation.screen.home.MainRouter

class HomeFragmentPresenter(
    internetConnectivityTracker: InternetConnectivityTracker,
    router: MainRouter
) : BaseMvpLcePresenter<HomeFragmentContract.Ui, HomeFragmentContract.Presenter.State, MainRouter>(
    internetConnectivityTracker, router
), HomeFragmentContract.Presenter {

    private var connectedlocationTracker: LocationTracker? = null

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

    override fun onTrackUpdated(track: Track) {

    }
}

    override fun start() {
        super.start()
        updateUi()
    }

    override fun startStopClicked() {
        //?.testMethod()

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
        connectedlocationTracker?.locationTrackerListener = trackingListener
        Log.i("qaz", "location tracker connected in presenter")
        updateUi()
    }

    override fun locationTrackerDisconnected() {
        connectedlocationTracker?.locationTrackerListener = null
        connectedlocationTracker = null
        updateUi()
    }

    override fun isLoading(): Boolean =
        false

    override fun reload() {

    }

    override fun updateUi() {
        super.updateUi()
        connectedlocationTracker?.let { tracker ->
            if (tracker.isRecording()) {
                ui.setButtonCaptionAsStop()
            }  else {
                ui.setButtonCaptionAsStart()
            }
        }
    }
}