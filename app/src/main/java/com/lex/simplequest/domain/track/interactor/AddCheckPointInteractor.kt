package com.lex.simplequest.domain.track.interactor

import com.lex.simplequest.domain.interactor.SingleResultInteractor
import com.lex.simplequest.domain.model.CheckPoint

interface AddCheckPointInteractor : SingleResultInteractor<AddCheckPointInteractor.Param, Unit> {
    data class Param(val checkPoint: CheckPoint)
}