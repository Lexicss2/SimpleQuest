package com.lex.simplequest.presentation.screen.home.home

import com.lex.simplequest.domain.locationmanager.LocationTracker
import com.lex.simplequest.presentation.base.BaseMvpContract
import com.lex.simplequest.presentation.base.BaseMvpLceContract

interface HomeFragmentContract {

    interface Ui : BaseMvpLceContract.Ui {
        fun setButtonCaptionAsStart()
        fun setButtonCaptionAsStop()
    }

    interface Presenter : BaseMvpLceContract.Presenter<Ui, Presenter.State> {
        fun startStopClicked()
        fun locationTrackerConnected(locationTracker: LocationTracker)
        fun locationTrackerDisconnected()

        interface State : BaseMvpLceContract.Presenter.State
    }
}