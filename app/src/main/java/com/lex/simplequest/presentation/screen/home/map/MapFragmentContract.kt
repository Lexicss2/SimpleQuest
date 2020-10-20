package com.lex.simplequest.presentation.screen.home.map

import com.lex.simplequest.presentation.base.BaseMvpContract

interface MapFragmentContract {
    interface Ui : BaseMvpContract.Ui {
        fun setPin(data: Any)
    }

    interface Presenter : BaseMvpContract.Presenter<Ui, Presenter.State> {

        interface State : BaseMvpContract.Presenter.State
    }
}