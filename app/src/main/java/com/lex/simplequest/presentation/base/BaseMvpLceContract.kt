package com.lex.simplequest.presentation.base

interface BaseMvpLceContract {

    interface Ui : BaseMvpContract.Ui {
        fun showLoading()
        fun showContent()
        fun showError(error: Throwable)
    }

    interface Presenter<U : Ui, S : Presenter.State> : BaseMvpContract.Presenter<U, S> {
        fun reload()

        interface State : BaseMvpContract.Presenter.State {
            var error: Throwable?
        }
    }
}