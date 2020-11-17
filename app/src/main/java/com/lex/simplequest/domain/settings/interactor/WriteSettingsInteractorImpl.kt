package com.lex.simplequest.domain.settings.interactor

import com.lex.simplequest.domain.interactor.RxSingleResultInteractor
import com.lex.simplequest.domain.repository.SettingsRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class WriteSettingsInteractorImpl(private val settingsRepository: SettingsRepository) :
    WriteSettingsInteractor,
    RxSingleResultInteractor<WriteSettingsInteractor.Param, WriteSettingsInteractor.Result>() {

    override fun createSingle(param: WriteSettingsInteractor.Param): Single<WriteSettingsInteractor.Result> =
        Single.just(param)
            .map {
                if (null != param.timePeriod) {
                    settingsRepository.setTimePeriod(param.timePeriod)
                }
                if (null != param.distance) {
                    settingsRepository.setRecordDistanceSensitivity(param.distance)
                }

                WriteSettingsInteractor.Result()
            }
            .subscribeOn(Schedulers.io())
}