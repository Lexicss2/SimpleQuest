package com.lex.simplequest.presentation.screen.home.tracks

import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.presentation.base.BaseMvpContract

interface TrackViewFragmentContract {
    interface Ui : BaseMvpContract.Ui {
        fun setTrack(track: Track) // Later set the array of tracks
    }

    interface Presenter : BaseMvpContract.Presenter<Ui, Presenter.State> {

        interface State : BaseMvpContract.Presenter.State
    }
}