package com.lex.simplequest.presentation.screen.home.map

import com.lex.simplequest.domain.common.connectivity.InternetConnectivityTracker
import com.lex.simplequest.domain.locationmanager.LocationTracker
import com.lex.simplequest.domain.locationmanager.model.Location
import com.lex.simplequest.presentation.base.BaseMvpPresenter
import com.lex.simplequest.presentation.screen.home.MainRouter

class MapFragmentPresenter(
    internetConnectivityTracker: InternetConnectivityTracker,
    router: MainRouter
) : BaseMvpPresenter<MapFragmentContract.Ui, MapFragmentContract.Presenter.State, MainRouter>(router),
    MapFragmentContract.Presenter {

    companion object {
        private const val FLAG_LOCATION = 0x01
    }

    private var location: Location? = null
    private var connectedlocationTracker: LocationTracker? = null

    private val trackingListener = object : LocationTracker.Listener {
        override fun onLocationManagerConnected() {

        }

        override fun onLocationMangerConnectionSuspended(reason: Int) {

        }

        override fun onLocationMangerConnectionFailed(error: Throwable) {

        }

        override fun onLocationUpdated(location: Location) {
            this@MapFragmentPresenter.location = location
            updateUi(FLAG_LOCATION)
        }
    }

    override fun saveState(state: MapFragmentContract.Presenter.State) {
        super.saveState(state)
        location?.let {
            state.location = it
        }
    }

    override fun restoreState(savedState: MapFragmentContract.Presenter.State?) {
        super.restoreState(savedState)
        savedState?.let {
            location = it.location
        }
    }

    // TODO: Check if recording track or not.
    // If not - just show Current location
    // If recording - show location and track
    override fun locationTrackerConnected(locationTracker: LocationTracker) {
        connectedlocationTracker = locationTracker
        connectedlocationTracker?.addListener(trackingListener)
        connectedlocationTracker?.connect()
        updateUi(0)
    }

    override fun locationTrackerDisconnected() {
        connectedlocationTracker?.disconnect()
        connectedlocationTracker?.removeListener(trackingListener)
        connectedlocationTracker = null
        updateUi(0)
    }

    override fun mapReady() {
        updateUi(FLAG_LOCATION)
    }

    override fun refreshClicked() {
        location?.let {
            ui.updateMarker(it)
        }
    }

    private fun updateUi(flag: Int) {
        if (flag and FLAG_LOCATION != 0) {
            location?.let {
                ui.showMarkerIfNeeded(it)
            }
        }
    }
}