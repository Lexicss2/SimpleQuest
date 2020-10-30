package com.lex.simplequest.presentation.screen.home.home

import com.lex.simplequest.domain.locationmanager.LocationTracker
import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.presentation.base.BaseMvpContract
import com.lex.simplequest.presentation.base.BaseMvpLceContract

interface HomeFragmentContract {

    interface Ui : BaseMvpLceContract.Ui {
        fun setButtonStyleRecording(recording: Boolean)
        fun showLastTrackInfo(track: Track?, isRecording: Boolean)
        fun showProgress(show: Boolean)
        fun setDurationMinutesSeconds(minutes: String, seconds: String)
    }

    interface Presenter : BaseMvpLceContract.Presenter<Ui, Presenter.State> {
        fun startStopClicked()
        fun locationTrackerConnected(locationTracker: LocationTracker)
        fun locationTrackerDisconnected()

        interface State : BaseMvpLceContract.Presenter.State
    }
}