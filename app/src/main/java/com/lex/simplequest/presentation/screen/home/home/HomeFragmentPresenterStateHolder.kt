package com.lex.simplequest.presentation.screen.home.home

import android.os.Bundle
import com.softeq.android.mvp.PresenterStateHolder

class HomeFragmentPresenterStateHolder : PresenterStateHolder<HomeFragmentContract.Presenter.State> {

    companion object {
        private const val ERROR = "error"
    }

    override fun save(state: HomeFragmentContract.Presenter.State, bundle: Bundle) {
        state.error?.let { bundle.putSerializable(ERROR, it) }
    }

    override fun create(): HomeFragmentContract.Presenter.State? =
        State()

    override fun restore(bundle: Bundle?): HomeFragmentContract.Presenter.State? =
        bundle?.let {
            State().apply {
                error = it.getSerializable(ERROR) as? Throwable
            }
        }

    class State : HomeFragmentContract.Presenter.State {
        override var error: Throwable? = null
    }
}