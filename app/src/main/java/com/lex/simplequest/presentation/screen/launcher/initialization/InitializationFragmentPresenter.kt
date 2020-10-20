package com.lex.simplequest.presentation.screen.launcher.initialization

import com.lex.core.log.LogFactory
import com.lex.simplequest.domain.application.interactor.StartApplicationInteractor
import com.lex.simplequest.presentation.base.BaseMvpPresenter
import com.lex.simplequest.presentation.screen.launcher.LauncherRouter
import com.lex.simplequest.presentation.utils.asRxSingle
import com.lex.simplequest.presentation.utils.tasks.SingleResultTask
import io.reactivex.android.schedulers.AndroidSchedulers

class InitializationFragmentPresenter(
    private val startApplicationInteractor: StartApplicationInteractor,
    logFactory: LogFactory,
    router: LauncherRouter
) : BaseMvpPresenter<InitializationFragmentContract.Ui, InitializationFragmentContract.Presenter.State, LauncherRouter>(
    router
), InitializationFragmentContract.Presenter {

    companion object {
        private const val TAG = "InitializationFragmentPresenter"
        private const val TASK_START_APPLICATION = "startApplication"
    }

    private var initializationState =
        InitializationFragmentContract.InitializationState.INITIALIZING
    private val log = logFactory.get(TAG)
    private val taskStartApplication = createStartApplicationTask()

    override fun saveState(state: InitializationFragmentContract.Presenter.State) {
        super.saveState(state)
        state.initializationState = initializationState
    }

    override fun restoreState(savedState: InitializationFragmentContract.Presenter.State?) {
        super.restoreState(savedState)
        if (null != savedState) {
            initializationState = savedState.initializationState
        }
    }

    override fun start() {
        super.start()
        when(initializationState) {
            InitializationFragmentContract.InitializationState.INITIALIZING -> {
                taskStartApplication.start(StartApplicationInteractor.Param.Default, Unit)
            }

            else -> {
                // do nothing
            }
        }
        updateUi()
    }

    override fun stop() {
        super.stop()
        taskStartApplication.stop()
    }

    override fun locationPermissionsUpdated() {
        taskStartApplication.stop()
        taskStartApplication.start(StartApplicationInteractor.Param.LocationPermissionUpdated, Unit)
        updateUi()
    }

    override fun locationServicesUpdated(locationServicesShouldBeEnabled: Boolean) {
        taskStartApplication.stop()
        taskStartApplication.start(
            StartApplicationInteractor.Param.LocationServicesUpdated(
                locationServicesShouldBeEnabled
            ), Unit
        )
        updateUi()
    }

    override fun errorPopupDismissed() {
        taskStartApplication.stop()
        taskStartApplication.start(StartApplicationInteractor.Param.Default, Unit)
        updateUi()
    }

    private fun updateUi() {
        ui.showProgress(taskStartApplication.isRunning())
    }

    private fun handleApplicationStartTaskComplete(
        result: StartApplicationInteractor.Result?,
        error: Throwable?
    ) {
        updateUi()
        initializationState =
            when {
                null != result -> {
                    when (result) {
                        is StartApplicationInteractor.Result.AskLocationPermission -> {
                            ui.requestLocationPermissions()
                            InitializationFragmentContract.InitializationState.REQUESTING_LOCATION_PERMISSION
                        }
                        is StartApplicationInteractor.Result.AskEnableLocationServices -> {
                            ui.requestEnableLocationServices()
                            InitializationFragmentContract.InitializationState.REQUESTING_ENABLE_LOCATION_SERVICES
                        }
//                        is StartApplicationInteractor.Result.AskSelectSiteManually -> {
//                            router.showSelectLocation(
//                                null,
//                                false,
//                                InitializationFragmentContract.TAG_SELECT_LOCATION_RESULT
//                            )
//                            InitializationFragmentContract.InitializationState.SELECTING_SITE_MANUALLY
//                        }
                        is StartApplicationInteractor.Result.Success -> {
                            router.launchHomeScreen()
                            InitializationFragmentContract.InitializationState.COMPLETE
                        }
                    }
                }
                null != error -> {
                    ui.showErrorPopup(error)
                    InitializationFragmentContract.InitializationState.FAILURE
                }
                else -> throw IllegalStateException("Should never happens")
            }
    }

    private fun createStartApplicationTask() =
        SingleResultTask<StartApplicationInteractor.Param, StartApplicationInteractor.Result, Unit>(
            TASK_START_APPLICATION,
            { param, _ ->
                startApplicationInteractor.asRxSingle(param)
                    .observeOn(AndroidSchedulers.mainThread())
            },
            { data, _ ->
                handleApplicationStartTaskComplete(data, null)
            },
            { error, _ ->
                log.e(error, "Failed to start application")
                handleApplicationStartTaskComplete(null, error)
            })
}
