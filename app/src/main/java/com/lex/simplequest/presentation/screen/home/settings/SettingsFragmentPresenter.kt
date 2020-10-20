package com.lex.simplequest.presentation.screen.home.settings

import com.lex.simplequest.domain.common.connectivity.InternetConnectivityTracker
import com.lex.simplequest.presentation.base.BaseMvpPresenter
import com.lex.simplequest.presentation.screen.home.MainRouter

class SettingsFragmentPresenter(
    internetConnectivityTracker: InternetConnectivityTracker,
    router: MainRouter
) : BaseMvpPresenter<SettingsFragmentContract.Ui, SettingsFragmentContract.Presenter.State, MainRouter>(
    router
),
    SettingsFragmentContract.Presenter {

}