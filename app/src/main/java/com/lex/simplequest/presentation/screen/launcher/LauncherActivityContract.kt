package com.lex.simplequest.presentation.screen.launcher

import com.lex.simplequest.presentation.base.BaseMvpContract

interface LauncherActivityContract {

    interface Ui : BaseMvpContract.Ui

    interface Presenter : BaseMvpContract.Presenter<Ui, Presenter.State> {

        interface State : BaseMvpContract.Presenter.State
    }
}