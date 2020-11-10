package com.lex.simplequest.domain.settings.interactor

import com.lex.simplequest.domain.interactor.SingleResultInteractor

interface ReadSettingsInteractor :
    SingleResultInteractor<ReadSettingsInteractor.Param, ReadSettingsInteractor.Result> {
    class Param
    data class Result(val timePeriod: Long)
}