package com.lex.simplequest.presentation.screen.home.tracks.map

import com.lex.simplequest.domain.locationmanager.model.Location
import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.presentation.base.BaseMvpContract

interface TrackViewFragmentContract {
    interface Ui : BaseMvpContract.Ui {
        fun showStartMarker(location: Location?)
        fun showFinishMarker(location: Location?, shouldMoveCamera: Boolean = false)
        fun showTrack(
            track: Track?,
            shouldMoveCamera: Boolean = false
        ) // Later set the array of tracks

        fun showError(error: Throwable)
    }

    interface Presenter : BaseMvpContract.Presenter<Ui, Presenter.State> {
        fun mapReady()
        fun detailsClicked()
        interface State : BaseMvpContract.Presenter.State
    }
}