package com.lex.simplequest.domain.track.interactor

import android.util.Log
import com.lex.simplequest.domain.interactor.RxMultiResultInteractor
import com.lex.simplequest.domain.repository.LocationRepository
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import com.lex.simplequest.domain.utils.asRxObservable

class AddPointInteractorImpl(
    private val locationRepository: LocationRepository
) : AddPointInteractor, RxMultiResultInteractor<AddPointInteractor.Param, Unit>() {
    override fun createObservable(param: AddPointInteractor.Param): Observable<Unit> =
        param.pointObservableValue.asRxObservable()
            .observeOn(Schedulers.io())
            .map { point ->
                if (point.trackId != -1L) {
                    locationRepository.addPoint(
                        point.trackId,
                        point.latitude,
                        point.longitude,
                        point.altitude
                    )
                } else {
                    Log.w("qaz", "Skip cause it is default")
                }

            }


}