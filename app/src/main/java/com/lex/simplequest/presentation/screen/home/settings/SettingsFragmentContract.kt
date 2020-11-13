package com.lex.simplequest.presentation.screen.home.settings

import com.lex.simplequest.presentation.base.BaseMvpContract
import com.lex.simplequest.presentation.base.BaseMvpLceContract

interface SettingsFragmentContract {
    interface Ui : BaseMvpLceContract.Ui {
        fun showProgress(show: Boolean)
        fun showTimePeriod(timePeriodMs: Long?)
        fun showAccuracyPopup(timePeriodMs: Long?, periods: Array<String>)
    }

    interface Presenter : BaseMvpLceContract.Presenter<Ui, Presenter.State> {
        fun selectedTimePeriod(timePeriodMs: Long)

        fun accuracyClicked()
        interface State : BaseMvpLceContract.Presenter.State
    }
}