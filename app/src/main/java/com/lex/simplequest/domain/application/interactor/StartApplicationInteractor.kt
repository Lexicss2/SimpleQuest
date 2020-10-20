package com.lex.simplequest.domain.application.interactor

import com.lex.simplequest.domain.interactor.SingleResultInteractor

interface StartApplicationInteractor :
    SingleResultInteractor<StartApplicationInteractor.Param, StartApplicationInteractor.Result> {
    sealed class Param {
        object Default : Param()
        object LocationPermissionUpdated : Param()
        data class LocationServicesUpdated(val locationServicesShouldBeEnabled: Boolean) : Param()
    }

    sealed class Result {
        object AskLocationPermission : Result()
        object AskEnableLocationServices : Result()
        object Success : Result()
    }
}