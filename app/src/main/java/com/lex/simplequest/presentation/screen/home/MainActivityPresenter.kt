package com.lex.simplequest.presentation.screen.home

import com.lex.simplequest.presentation.base.BaseMvpPresenter

class MainActivityPresenter(router: MainRouter) :
    BaseMvpPresenter<MainActivityContract.Ui, MainActivityContract.Presenter.State, MainRouter>(
        router
    ), MainActivityContract.Presenter {

    override fun onNavigationHomeClicked() {
        router.showHome()
    }

    override fun onNavigationMapClicked() {
        router.showMap()
    }

    override fun onNavigationTrackListClicked() {
        router.showTracks()
    }

    override fun onNavigationSettingsClicked() {
        router.showSettings()
    }
}