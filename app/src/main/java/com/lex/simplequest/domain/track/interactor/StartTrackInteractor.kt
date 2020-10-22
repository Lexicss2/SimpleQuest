package com.lex.simplequest.domain.track.interactor

import com.lex.simplequest.domain.interactor.SingleResultInteractor

interface StartTrackInteractor :
    SingleResultInteractor<StartTrackInteractor.Param, StartTrackInteractor.Result> {
    data class Param(val name: String, val startTime: Long)

    data class Result(val trackId: Long)
}