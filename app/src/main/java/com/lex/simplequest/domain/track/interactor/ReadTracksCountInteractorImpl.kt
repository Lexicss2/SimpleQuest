package com.lex.simplequest.domain.track.interactor

import android.util.Log
import com.lex.simplequest.domain.interactor.RxSingleResultInteractor
import com.lex.simplequest.domain.repository.LocationRepository
import io.reactivex.Single

class ReadTracksCountInteractorImpl(
    private val locationRepository: LocationRepository
) : ReadTracksCountInteractor,
    RxSingleResultInteractor<ReadTracksCountInteractor.Param, ReadTracksCountInteractor.Result>() {

    override fun createSingle(param: ReadTracksCountInteractor.Param): Single<ReadTracksCountInteractor.Result> =
        Single.just(true)
            .map {
                val startTime = System.currentTimeMillis()
                val count = locationRepository.getTracksCount()
                val delta = System.currentTimeMillis() - startTime
                ReadTracksCountInteractor.Result(count)
            }
}