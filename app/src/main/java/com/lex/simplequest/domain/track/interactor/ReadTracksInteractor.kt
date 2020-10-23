package com.lex.simplequest.domain.track.interactor

import com.lex.simplequest.domain.interactor.SingleResultInteractor
import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.domain.repository.LocationRepository

interface ReadTracksInteractor :
    SingleResultInteractor<ReadTracksInteractor.Param, ReadTracksInteractor.Result> {
    data class Param(val spec: LocationRepository.LocationQuerySpecification)

    data class Result(val tracks: List<Track>)
}