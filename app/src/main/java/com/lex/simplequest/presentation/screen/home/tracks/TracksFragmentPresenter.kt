package com.lex.simplequest.presentation.screen.home.tracks

import com.lex.simplequest.domain.common.connectivity.InternetConnectivityTracker
import com.lex.simplequest.presentation.base.BaseMvpPresenter
import com.lex.simplequest.presentation.screen.home.MainRouter

class TracksFragmentPresenter(
    internetConnectivityTracker: InternetConnectivityTracker,
    router: MainRouter
) : BaseMvpPresenter<TracksFragmentContract.Ui, TracksFragmentContract.Presenter.State, MainRouter>(
    router
),
    TracksFragmentContract.Presenter {

}