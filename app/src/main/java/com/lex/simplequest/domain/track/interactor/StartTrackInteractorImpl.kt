package com.lex.simplequest.domain.track.interactor

import com.lex.simplequest.domain.interactor.RxSingleResultInteractor
import com.lex.simplequest.domain.repository.LocationRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class StartTrackInteractorImpl(
    private val locationRepository: LocationRepository
) : StartTrackInteractor, RxSingleResultInteractor<StartTrackInteractor.Param, StartTrackInteractor.Result>() {

    override fun createSingle(param: StartTrackInteractor.Param): Single<StartTrackInteractor.Result> =
        Single.just(true)
            .map {
                val id = locationRepository.startTrack(param.name, param.startTime)
                StartTrackInteractor.Result(id)
            }
            .subscribeOn(Schedulers.io())
}