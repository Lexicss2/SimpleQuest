package com.lex.simplequest.presentation.screen.home

import com.lex.core.log.LogFactory
import com.lex.simplequest.domain.locationmanager.LocationTracker
import com.lex.simplequest.presentation.base.BaseMvpPresenter

class MainActivityPresenter(
    logFactory: LogFactory,
    router: MainRouter
) :
    BaseMvpPresenter<MainActivityContract.Ui, MainActivityContract.Presenter.State, MainRouter>(
        router
    ), MainActivityContract.Presenter {

    companion object {
        private const val TAG = "MainActivityPresenter"
        private const val TASK_READ_SETTINGS = "task_read_settings"
    }

    private val log = logFactory.get(TAG)
    private var locationTracker: LocationTracker? = null
    private var isTrackRecording: Boolean = false

    override fun onNavigationHomeClicked() {
        router.showHome()
    }

    override fun onNavigationMapClicked() {
        router.showMap()
    }

    override fun onNavigationTrackListClicked() {
        router.showTracks()
    }

    override fun onNavigationSettingsClicked() {
        router.showSettings()
    }

    override fun create() {
        ui.startLocationTracker()
    }

    override fun resume() {
        ui.bindLocationTracker()
    }

    override fun pause() {
        isTrackRecording = locationTracker?.isRecording() ?: false
        ui.unbindLocationTracker()
    }

    override fun destroy() {
        if (!isTrackRecording) {
            ui.stopLocationTracker()
        }
    }

    override fun serviceConnected(locationTracker: LocationTracker) {
        this.locationTracker = locationTracker
    }

    override fun serviceDisconnected() {
        this.locationTracker = null
    }
}