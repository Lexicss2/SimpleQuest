package com.lex.simplequest.presentation.screen.home.home

import android.util.Log
import com.lex.simplequest.domain.common.connectivity.InternetConnectivityTracker
import com.lex.simplequest.domain.locationmanager.LocationTracker
import com.lex.simplequest.domain.locationmanager.model.Location
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

        override fun onLocationUpdated(location: Location) {
            updateUi()
        }
    }

    override fun start() {
        super.start()
        updateUi()
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
        connectedlocationTracker?.let { tracker ->
            if (tracker.isRecording()) {
                ui.setButtonCaptionAsStop()
            } else {
                ui.setButtonCaptionAsStart()
            }
        }
    }
}