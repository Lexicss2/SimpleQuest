package com.lex.simplequest.presentation.screen.home.tracks.list

import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.presentation.base.BaseMvpContract

interface TracksFragmentContract {
    interface Ui : BaseMvpContract.Ui {
        fun setTracks(items: List<Track>)
        fun showNoContent()
        fun showProgress(show: Boolean)
        fun showError(error: Throwable)
    }

    interface Presenter : BaseMvpContract.Presenter<Ui, Presenter.State> {
        fun trackClicked(track: Track)
        fun trackInfoClicked(track: Track)

        interface State : BaseMvpContract.Presenter.State
    }
}