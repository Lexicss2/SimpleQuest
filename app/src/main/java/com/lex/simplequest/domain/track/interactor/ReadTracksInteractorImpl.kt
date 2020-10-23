package com.lex.simplequest.domain.track.interactor

import com.lex.core.utils.Cancellable
import com.lex.simplequest.domain.interactor.RxSingleResultInteractor
import com.lex.simplequest.domain.interactor.SingleResultInteractor
import com.lex.simplequest.domain.repository.LocationRepository
import io.reactivex.Single

class ReadTracksInteractorImpl(
    private val locationRepository: LocationRepository
) : ReadTracksInteractor,
    RxSingleResultInteractor<ReadTracksInteractor.Param, ReadTracksInteractor.Result>() {

    override fun createSingle(param: ReadTracksInteractor.Param): Single<ReadTracksInteractor.Result> =
        Single.just(true)
            .map {
                val list = locationRepository.getTracks(param.spec)
                ReadTracksInteractor.Result(list)
            }
}