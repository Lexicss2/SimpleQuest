package com.lex.simplequest.domain.settings.interactor

import com.lex.simplequest.domain.interactor.RxSingleResultInteractor
import com.lex.simplequest.domain.repository.SettingsRepository
import com.lex.simplequest.domain.track.interactor.ReadTracksInteractor
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class ReadSettingsInteractorImpl(private val settingsRepository: SettingsRepository) :
    ReadSettingsInteractor,
    RxSingleResultInteractor<ReadSettingsInteractor.Param, ReadSettingsInteractor.Result>() {

    override fun createSingle(param: ReadSettingsInteractor.Param): Single<ReadSettingsInteractor.Result> =
        Single.just(param)
            .map {
                val timePeriod = settingsRepository.getTimePeriod()
                ReadSettingsInteractor.Result(timePeriod)
            }
            .subscribeOn(Schedulers.io())
}