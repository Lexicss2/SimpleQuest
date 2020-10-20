package com.lex.simplequest.presentation.screen.home.home

import com.lex.simplequest.device.service.TrackLocationService
import com.lex.simplequest.domain.common.connectivity.InternetConnectivityTracker
import com.lex.simplequest.domain.locationmanager.LocationTracker
import com.lex.simplequest.presentation.base.BaseMvpLcePresenter
import com.lex.simplequest.presentation.screen.home.MainRouter

class HomeFragmentPresenter(
    internetConnectivityTracker: InternetConnectivityTracker,
    router: MainRouter
) : BaseMvpLcePresenter<HomeFragmentContract.Ui, HomeFragmentContract.Presenter.State, MainRouter>(
    internetConnectivityTracker, router
), HomeFragmentContract.Presenter {

    private var connectedlocationTracker: LocationTracker? = null

    override fun testClicked() {
        connectedlocationTracker?.testMethod()
    }

    override fun locationTrackerConnected(locationTracker: LocationTracker) {
        connectedlocationTracker = locationTracker
    }

    override fun locationTrackerDisconnected() {
        connectedlocationTracker = null
    }

    override fun isLoading(): Boolean =
        false

    override fun reload() {

    }
}