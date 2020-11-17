package com.lex.simplequest.presentation.screen.home.settings

import com.lex.simplequest.presentation.base.BaseMvpContract
import com.lex.simplequest.presentation.base.BaseMvpLceContract

interface SettingsFragmentContract {
    interface Ui : BaseMvpLceContract.Ui {
        fun showProgress(show: Boolean)
        fun showTimePeriod(timePeriodMs: Long?)
        fun showDistance(distance: Long?)
        fun showGpsAccuracyPopup(timePeriodMs: Long?, periods: Array<String>)
        fun showTrackSensitivityPopup(distance: Long?, distances: Array<String>)
    }

    interface Presenter : BaseMvpLceContract.Presenter<Ui, Presenter.State> {
        fun selectedTimePeriod(timePeriodMs: Long)
        fun gpsAccuracyClicked()
        fun selectDistance(distance: Long)
        fun trackSensitivityClicked()

        interface State : BaseMvpLceContract.Presenter.State
    }
}