package com.lex.simplequest.presentation.screen.home.home

import com.lex.simplequest.domain.locationmanager.LocationTracker
import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.presentation.base.BaseMvpLceContract

interface HomeFragmentContract {

    interface Ui : BaseMvpLceContract.Ui {
        fun setButtonStyleRecording(recordButtonType: RecordButtonType?)
        fun showLastTrackName(trackName: String?, isRecording: Boolean)
        fun showProgress(show: Boolean)
        fun showLastTrackDuration(minutes: String?, seconds: String?)
        fun showLastTrackDistance(distance: String?, withBoldStyle: Boolean)
        fun setLocationAvailableStatus(isAvailable: Boolean?)
        fun setLocationSuspendedStatus(reason: Int?)
        fun setError(error: Throwable?)
        fun setTrackerStatus(status: LocationTracker.Status?, tag: String?)
    }

    interface Presenter : BaseMvpLceContract.Presenter<Ui, Presenter.State> {
        fun startStopClicked()
        fun pauseResumeClicked()
        fun locationTrackerServiceConnected(locationTracker: LocationTracker)
        fun locationTrackerServiceDisconnected()

        interface State : BaseMvpLceContract.Presenter.State {
            var isLocationAvailable: Boolean?
            var locationSuspendedReason: Int?
        }
    }
}