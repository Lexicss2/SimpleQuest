package com.lex.simplequest.domain.track.interactor

import android.util.Log
import com.lex.core.utils.Cancellable
import com.lex.simplequest.domain.interactor.RxSingleResultInteractor
import com.lex.simplequest.domain.interactor.SingleResultInteractor
import com.lex.simplequest.domain.repository.LocationRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class ReadTracksInteractorImpl(
    private val locationRepository: LocationRepository
) : ReadTracksInteractor,
    RxSingleResultInteractor<ReadTracksInteractor.Param, ReadTracksInteractor.Result>() {

    override fun createSingle(param: ReadTracksInteractor.Param): Single<ReadTracksInteractor.Result> =
        Single.just(true)
            .map {
                val startTime = System.currentTimeMillis()
                val list = locationRepository.getTracks(param.spec)
                val delta = System.currentTimeMillis() - startTime
                Log.i("qaz", "calculated delta = $delta")
                ReadTracksInteractor.Result(list)
            }
            .subscribeOn(Schedulers.io())
}