package com.lex.simplequest.presentation.screen.home

import com.lex.simplequest.domain.locationmanager.LocationTracker
import com.lex.simplequest.presentation.base.BaseMvpContract

interface MainActivityContract {

    interface Ui : BaseMvpContract.Ui {
        fun startLocationTracker(): Any?
        fun bindLocationTracker(): Boolean
        fun unbindLocationTracker()
        fun stopLocationTracker()
    }

    interface Presenter : BaseMvpContract.Presenter<Ui, Presenter.State> {

        fun onNavigationHomeClicked()
        fun onNavigationMapClicked()
        fun onNavigationTrackListClicked()
        fun onNavigationSettingsClicked()

        fun create()
        fun resume()
        fun pause()
        fun destroy()
        fun serviceConnected(locationTracker: LocationTracker)
        fun serviceDisconnected()

        interface State : BaseMvpContract.Presenter.State
    }
}