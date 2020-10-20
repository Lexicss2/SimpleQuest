package com.lex.simplequest.presentation.screen.home.map

import com.lex.simplequest.domain.common.connectivity.InternetConnectivityTracker
import com.lex.simplequest.presentation.base.BaseMvpPresenter
import com.lex.simplequest.presentation.screen.home.MainRouter

class MapFragmentPresenter(
    internetConnectivityTracker: InternetConnectivityTracker,
    router: MainRouter
) : BaseMvpPresenter<MapFragmentContract.Ui, MapFragmentContract.Presenter.State, MainRouter>(router),
    MapFragmentContract.Presenter {

}