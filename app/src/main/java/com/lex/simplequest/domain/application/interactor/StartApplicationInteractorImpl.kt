package com.lex.simplequest.domain.application.interactor

import com.lex.simplequest.domain.interactor.RxSingleResultInteractor
import com.lex.simplequest.domain.permission.repository.PermissionChecker
import com.lex.simplequest.domain.repository.LocationRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers


class StartApplicationInteractorImpl(
    private val permissionChecker: PermissionChecker,
    private val locationRepository: LocationRepository
) :
    RxSingleResultInteractor<StartApplicationInteractor.Param, StartApplicationInteractor.Result>(),
    StartApplicationInteractor {

    companion object {
        private val LOCATION_PERMISSIONS = setOf(
            PermissionChecker.Permission.ACCESS_FINE_LOCATION,
            PermissionChecker.Permission.ACCESS_COARSE_LOCATION
        )
    }

    override fun createSingle(param: StartApplicationInteractor.Param): Single<StartApplicationInteractor.Result> =
        Single.just(true)
            .observeOn(Schedulers.computation())
            .map {
                when {
                    permissionChecker.checkAllPermissionGranted(LOCATION_PERMISSIONS) -> when {
                        locationRepository.isLocationServicesEnabled() -> {
                            StartApplicationInteractor.Result.Success
                        }
                        param !is StartApplicationInteractor.Param.LocationServicesUpdated -> {
                            StartApplicationInteractor.Result.AskEnableLocationServices
                        }
                        else -> if (param.locationServicesShouldBeEnabled) {
                            StartApplicationInteractor.Result.AskEnableLocationServices
                        } else {
                            StartApplicationInteractor.Result.Success
                        }
                    }

                    param !is StartApplicationInteractor.Param.LocationPermissionUpdated -> {
                        StartApplicationInteractor.Result.AskLocationPermission
                    }

                    else -> {
                        StartApplicationInteractor.Result.Success
                    }
                }
            }
}