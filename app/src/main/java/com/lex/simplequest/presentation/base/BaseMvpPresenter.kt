package com.lex.simplequest.presentation.base

import com.softeq.android.mvp.BasePresenter

abstract class BaseMvpPresenter<U : BaseMvpContract.Ui, S : BaseMvpContract.Presenter.State, R : Router>(protected val router: R) :
    BasePresenter<U, S>(), BaseMvpContract.Presenter<U, S> {

    override fun start() {

    }

    override fun stop() {

    }

    override fun destroy(willBeRecreated: Boolean) {

    }

    override fun saveState(state: S) {

    }

    override fun restoreState(savedState: S?) {

    }
}