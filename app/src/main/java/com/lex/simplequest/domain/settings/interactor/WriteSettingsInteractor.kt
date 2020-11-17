package com.lex.simplequest.domain.settings.interactor

import com.lex.simplequest.domain.interactor.SingleResultInteractor

interface WriteSettingsInteractor :
    SingleResultInteractor<WriteSettingsInteractor.Param, WriteSettingsInteractor.Result> {
    data class Param(
        val timePeriod: Long?,
        val distance: Long?,
        val displacement: Long?,
        val batteryLevel: Int?
    )

    class Result
}