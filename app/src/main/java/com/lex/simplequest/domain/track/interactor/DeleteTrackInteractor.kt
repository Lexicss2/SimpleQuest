package com.lex.simplequest.domain.track.interactor

import com.lex.simplequest.domain.interactor.SingleResultInteractor

interface DeleteTrackInteractor : SingleResultInteractor<DeleteTrackInteractor.Param, DeleteTrackInteractor.Result> {
    data class Param(val trackId: Long)
    data class Result(val succeeded: Boolean)
}