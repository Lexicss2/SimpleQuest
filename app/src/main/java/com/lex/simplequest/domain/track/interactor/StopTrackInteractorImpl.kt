package com.lex.simplequest.domain.track.interactor

import com.lex.simplequest.domain.interactor.RxSingleResultInteractor
import com.lex.simplequest.domain.repository.LocationRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class StopTrackInteractorImpl(private val locationRepository: LocationRepository) :
    StopTrackInteractor,
    RxSingleResultInteractor<StopTrackInteractor.Param, StopTrackInteractor.Result>() {

    override fun createSingle(param: StopTrackInteractor.Param): Single<StopTrackInteractor.Result> =
        Single.just(true)
            .map {
                val succeeded = locationRepository.stopTrack(param.id, param.endTime)
                StopTrackInteractor.Result(succeeded)
            }
            .subscribeOn(Schedulers.io())
}