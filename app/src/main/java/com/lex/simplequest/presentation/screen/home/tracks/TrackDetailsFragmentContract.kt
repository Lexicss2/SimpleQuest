package com.lex.simplequest.presentation.screen.home.tracks

import com.lex.simplequest.presentation.base.BaseMvpContract

interface TrackDetailsFragmentContract {
    interface Ui : BaseMvpContract.Ui {
        fun showProgress(show: Boolean)
        fun setName(trackName: String?)
        fun setDistance(distance: String?)
        fun setSpeed(speed: String?)
        fun setDuration(duration: String?)
    }

    interface Presenter : BaseMvpContract.Presenter<Ui, Presenter.State> {
        fun nameChanged(name: String)
        fun shareClicked()
        fun deleteClicked()
        fun deleteConfirmed()

        interface State : BaseMvpContract.Presenter.State {
            var name: String?
        }
    }
}