package com.lex.simplequest.presentation.screen.home.home

import com.lex.simplequest.domain.locationmanager.LocationTracker
import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.presentation.base.BaseMvpLceContract

interface HomeFragmentContract {

    interface Ui : BaseMvpLceContract.Ui {
        fun setButtonStyleRecording(recordButtonType: RecordButtonType)
        fun showLastTrackInfo(track: Track?, isRecording: Boolean)
        fun showProgress(show: Boolean)
        fun setDurationMinutesSeconds(minutes: String, seconds: String)
        fun setLocationAvailableStatus(isAvailable: Boolean?)
        fun setLocationSuspendedStatus(reason: Int?)
        fun setError(error: Throwable?)
        fun setTrackerStatus(status: LocationTracker.Status?)
    }

    interface Presenter : BaseMvpLceContract.Presenter<Ui, Presenter.State> {
        fun startStopClicked()
        fun locationTrackerConnected(locationTracker: LocationTracker)
        fun locationTrackerDisconnected()

        interface State : BaseMvpLceContract.Presenter.State {
            var isLocationAvailable: Boolean?
            var locationSuspendedReason: Int?
        }
    }
}