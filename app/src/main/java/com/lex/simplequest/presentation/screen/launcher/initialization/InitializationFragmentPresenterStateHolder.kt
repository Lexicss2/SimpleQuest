package com.lex.simplequest.presentation.screen.launcher.initialization

import android.os.Bundle
import com.softeq.android.mvp.PresenterStateHolder

class InitializationFragmentPresenterStateHolder :
    PresenterStateHolder<InitializationFragmentContract.Presenter.State> {

    companion object {
        private const val INITIALIZATION_STATE = "initializationState"
    }

    override fun create(): InitializationFragmentContract.Presenter.State? =
        State()


    override fun save(state: InitializationFragmentContract.Presenter.State, bundle: Bundle) {
        bundle.putString(INITIALIZATION_STATE, state.initializationState.name)
    }

    override fun restore(bundle: Bundle?): InitializationFragmentContract.Presenter.State? =
        bundle?.let {
            State().apply {
                initializationState =
                    InitializationFragmentContract.InitializationState.valueOf(
                        bundle.getString(
                            INITIALIZATION_STATE
                        )!!
                    )
            }
        }

    private class State : InitializationFragmentContract.Presenter.State {
        override var initializationState: InitializationFragmentContract.InitializationState =
            InitializationFragmentContract.InitializationState.INITIALIZING
    }
}