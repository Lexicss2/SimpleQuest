package com.lex.simplequest.domain.track.interactor

import com.lex.simplequest.domain.interactor.SingleResultInteractor

interface StopTrackInteractor :
    SingleResultInteractor<StopTrackInteractor.Param, StopTrackInteractor.Result> {
    data class Param(val id: Long, val endTime: Long, val minimalDistance: Long?)
    data class Result(val succeeded: Boolean)
}