package com.lex.simplequest.presentation.screen.home.tracks

import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.presentation.base.BaseMvpContract

interface TracksFragmentContract {
    interface Ui : BaseMvpContract.Ui {
        fun setTracks(items: List<Track>)
    }

    interface Presenter : BaseMvpContract.Presenter<Ui, Presenter.State> {

        interface State : BaseMvpContract.Presenter.State
    }
}