package com.lex.simplequest.presentation.screen.home.settings

import com.lex.simplequest.presentation.base.BaseMvpContract

interface SettingsFragmentContract {
    interface Ui : BaseMvpContract.Ui {

    }

    interface Presenter : BaseMvpContract.Presenter<Ui, Presenter.State> {

        interface State : BaseMvpContract.Presenter.State
    }
}