package com.lex.simplequest.presentation.screen.home.tracks.details

import android.os.Bundle
import com.softeq.android.mvp.PresenterStateHolder

class TrackDetailsFragmentPresenterStateHolder : PresenterStateHolder<TrackDetailsFragmentContract.Presenter.State> {
    companion object {
        private const val NAME = "name"
    }

    override fun create(): TrackDetailsFragmentContract.Presenter.State? =
        State()


    override fun save(state: TrackDetailsFragmentContract.Presenter.State, bundle: Bundle) {
        state.name?.let {
            bundle.putString(NAME, it)
        }
    }

    override fun restore(bundle: Bundle?): TrackDetailsFragmentContract.Presenter.State? =
        bundle?.let {
            State().apply {
                name = it.getString(NAME, "")
            }
        }

    class State : TrackDetailsFragmentContract.Presenter.State {
        override var name: String? = null
    }
}