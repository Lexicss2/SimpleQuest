package com.lex.simplequest.presentation.screen.home

import com.lex.simplequest.presentation.base.BaseMvpContract

interface MainActivityContract {

    interface Ui : BaseMvpContract.Ui {

    }

    interface Presenter : BaseMvpContract.Presenter<Ui, Presenter.State> {

        fun onNavigationHomeClicked()
        fun onNavigationMapClicked()
        fun onNavigationTrackListClicked()
        fun onNavigationSettingsClicked()

        interface State : BaseMvpContract.Presenter.State
    }
}