package com.lex.simplequest.domain.track.interactor

import com.lex.simplequest.domain.interactor.RxMultiResultInteractor
import com.lex.simplequest.domain.repository.LocationRepository
import com.lex.simplequest.domain.utils.asRxObservable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class AddPointInteractorImpl(
    private val locationRepository: LocationRepository
) : AddPointInteractor, RxMultiResultInteractor<AddPointInteractor.Param, Unit>() {
    override fun createObservable(param: AddPointInteractor.Param): Observable<Unit> =
        param.pointObservableValue.asRxObservable()
            .observeOn(Schedulers.io())
            .map { point ->
                if (point.trackId != -1L) {
                    locationRepository.addPoint(point)
                }
            }


}