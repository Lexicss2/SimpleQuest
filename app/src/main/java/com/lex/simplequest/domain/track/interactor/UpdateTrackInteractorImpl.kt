package com.lex.simplequest.domain.track.interactor

import com.lex.simplequest.domain.interactor.RxSingleResultInteractor
import com.lex.simplequest.domain.repository.LocationRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class UpdateTrackInteractorImpl(
    private val locationRepository: LocationRepository
) : UpdateTrackInteractor, RxSingleResultInteractor<UpdateTrackInteractor.Param, UpdateTrackInteractor.Result>() {

    override fun createSingle(param: UpdateTrackInteractor.Param): Single<UpdateTrackInteractor.Result> =
        Single.just(true)
            .map {
                val result = locationRepository.updateTrack(param.track)
                if (result) {
                    UpdateTrackInteractor.Result(param.track)
                } else {
                    throw IllegalStateException("Track name ${param.track.name} was not updated")
                }
            }
            .subscribeOn(Schedulers.io())
}