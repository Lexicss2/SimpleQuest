package com.lex.simplequest.presentation.screen.home.map

import com.lex.simplequest.domain.locationmanager.LocationTracker
import com.lex.simplequest.domain.locationmanager.model.Location
import com.lex.simplequest.presentation.base.BaseMvpContract

interface MapFragmentContract {
    interface Ui : BaseMvpContract.Ui {
        fun showMarkerIfNeeded(location: Location)
        fun updateMarker(location: Location)
    }

    interface Presenter : BaseMvpContract.Presenter<Ui, Presenter.State> {
        fun locationTrackerConnected(locationTracker: LocationTracker)
        fun locationTrackerDisconnected()
        fun mapReady()
        fun refreshClicked()

        interface State : BaseMvpContract.Presenter.State {
            var location: Location?
        }
    }
}