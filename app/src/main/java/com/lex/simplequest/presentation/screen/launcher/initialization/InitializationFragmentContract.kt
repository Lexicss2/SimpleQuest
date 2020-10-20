package com.lex.simplequest.presentation.screen.launcher.initialization

import com.lex.simplequest.presentation.base.BaseMvpContract

interface InitializationFragmentContract {
    interface Ui : BaseMvpContract.Ui {
        fun showErrorPopup(error: Throwable)
        fun showProgress(show: Boolean)
        fun requestLocationPermissions()
        fun requestEnableLocationServices()
    }

    interface Presenter : BaseMvpContract.Presenter<Ui, Presenter.State> {

        fun locationPermissionsUpdated()
        fun locationServicesUpdated(locationServicesShouldBeEnabled: Boolean)
        fun errorPopupDismissed()

        interface State : BaseMvpContract.Presenter.State {
            var initializationState: InitializationState
        }
    }

    enum class InitializationState {
        INITIALIZING,
        REQUESTING_LOCATION_PERMISSION,
        REQUESTING_ENABLE_LOCATION_SERVICES,
        SELECTING_SITE_MANUALLY,
        COMPLETE,
        FAILURE
    }
}