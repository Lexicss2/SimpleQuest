package com.lex.simplequest.domain.track.interactor

import com.lex.simplequest.domain.interactor.RxSingleResultInteractor
import com.lex.simplequest.domain.repository.LocationRepository
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class AddCheckPointInteractorImpl(
    private val locationRepository: LocationRepository
) : AddCheckPointInteractor, RxSingleResultInteractor<AddCheckPointInteractor.Param, Unit>() {

    override fun createSingle(param: AddCheckPointInteractor.Param): Single<Unit> =
        Single.just(param)
            .map { p ->
                locationRepository.addCheckPoint(p.checkPoint)
            }
            .subscribeOn(Schedulers.io())
}