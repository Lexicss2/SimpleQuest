package com.lex.simplequest.domain.track.interactor

import com.lex.core.utils.ObservableValue
import com.lex.simplequest.domain.interactor.MultiResultInteractor
import com.lex.simplequest.domain.model.Point

interface AddPointInteractor : MultiResultInteractor<AddPointInteractor.Param, Unit> {
    data class Param(
        val pointObservableValue: ObservableValue<Point>
    )
}