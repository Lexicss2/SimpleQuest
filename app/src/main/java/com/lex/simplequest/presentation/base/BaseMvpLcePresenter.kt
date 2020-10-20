package com.lex.simplequest.presentation.base

import com.lex.simplequest.domain.common.connectivity.InternetConnectivityTracker
import com.lex.simplequest.presentation.utils.isInternetConnectivityError

abstract class BaseMvpLcePresenter<U : BaseMvpLceContract.Ui, S : BaseMvpLceContract.Presenter.State, R : Router>(
    private val internetConnectivityTracker: InternetConnectivityTracker,
    router: R
) :
    BaseMvpPresenter<U, S, R>(router), BaseMvpLceContract.Presenter<U, S> {

    protected var error: Throwable? = null

    override fun saveState(state: S) {
        super.saveState(state)
        state.error = error
    }

    override fun restoreState(savedState: S?) {
        super.restoreState(savedState)
        this.error = savedState?.error
    }

    override fun start() {
        super.start()
        internetConnectivityTracker.addInternetConnectivityChangedListener(internetConnectivityChangedListener)
        updateUi()
    }

    override fun stop() {
        super.stop()
        internetConnectivityTracker.removeInternetConnectivityChangedListener(internetConnectivityChangedListener)
    }

    abstract fun isLoading(): Boolean

    protected open fun updateUi() {
        if (isUiBinded) {
            val error = this.error
            when {
                isLoading() -> ui.showLoading()
                null != error -> ui.showError(error)
                else -> ui.showContent()
            }
        }
    }

    private val internetConnectivityChangedListener =
        object : InternetConnectivityTracker.OnInternetConnectivityChangedListener {
            override fun onInternetConnectivityChanged(internetConnectivityTracker: InternetConnectivityTracker) {
                val error = this@BaseMvpLcePresenter.error
                if (null != error && error.isInternetConnectivityError() && internetConnectivityTracker.isInternetConnected) {
                    reload()
                }
            }
        }
}