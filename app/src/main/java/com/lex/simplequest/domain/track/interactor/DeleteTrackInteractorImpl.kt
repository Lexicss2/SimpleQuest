package com.lex.simplequest.domain.track.interactor

import com.lex.simplequest.domain.interactor.RxSingleResultInteractor
import com.lex.simplequest.domain.repository.LocationRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class DeleteTrackInteractorImpl(
    private val locationRepository: LocationRepository
) : DeleteTrackInteractor, RxSingleResultInteractor<DeleteTrackInteractor.Param, DeleteTrackInteractor.Result>() {

    override fun createSingle(param: DeleteTrackInteractor.Param): Single<DeleteTrackInteractor.Result> =
        Single.just(true)
            .map {
                val result = locationRepository.deleteTrack(param.trackId)
                DeleteTrackInteractor.Result(result)
            }
            .subscribeOn(Schedulers.io())
}