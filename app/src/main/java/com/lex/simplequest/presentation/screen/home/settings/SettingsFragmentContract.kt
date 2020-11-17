package com.lex.simplequest.presentation.screen.home.settings

import com.lex.simplequest.presentation.base.BaseMvpLceContract

interface SettingsFragmentContract {

    interface Ui : BaseMvpLceContract.Ui {
        fun showProgress(show: Boolean)
        fun showTimePeriod(timePeriodMs: Long?)
        fun showDistance(distance: Long?)
        fun showDisplacement(displacement: Long?)
        fun showBatteryLevel(batteryLevel: Int?)
        fun showGpsAccuracyPopup(timePeriodMs: Long?, availablePeriods: Array<String>)
        fun showTrackSensitivityPopup(distance: Long?, availableDistances: Array<String>)
        fun showDisplacementPopup(displacement: Long?, availableDisplacements: Array<String>)
        fun showBatteryLevelPopup(batteryLevel: Int?, availableBatteryLevels: Array<String>)
        fun showAboutPopup()
    }

    interface Presenter : BaseMvpLceContract.Presenter<Ui, Presenter.State> {
        fun gpsAccuracyClicked()
        fun trackSensitivityClicked()
        fun displacementClicked()
        fun batteryLevelClicked()
        fun aboutClicked()
        fun selectedTimePeriod(timePeriodMs: Long)
        fun selectDistance(distance: Long)
        fun selectDisplacement(displacement: Long)
        fun selectBatteryLevel(batteryLevel: Int)

        interface State : BaseMvpLceContract.Presenter.State
    }
}