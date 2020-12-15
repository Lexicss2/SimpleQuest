package com.lex.simplequest.domain.track.interactor

import com.lex.simplequest.domain.interactor.SingleResultInteractor
import com.lex.simplequest.presentation.utils.tasks.SingleResultTask

interface ReadTracksCountInteractor :
    SingleResultInteractor<ReadTracksCountInteractor.Param, ReadTracksCountInteractor.Result> {
    class Param
    class Result(val count: Int)
}