package com.lex.simplequest.domain.track.interactor

import com.lex.simplequest.domain.interactor.SingleResultInteractor
import com.lex.simplequest.domain.model.Track

interface UpdateTrackInteractor : SingleResultInteractor<UpdateTrackInteractor.Param, UpdateTrackInteractor.Result> {
    data class Param(val track: Track)

    data class Result(val track: Track)
}