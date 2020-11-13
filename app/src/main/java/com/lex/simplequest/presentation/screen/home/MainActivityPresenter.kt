package com.lex.simplequest.presentation.screen.home

import android.util.Log
import com.lex.core.log.LogFactory
import com.lex.simplequest.domain.locationmanager.LocationTracker
import com.lex.simplequest.domain.settings.interactor.ReadSettingsInteractor
import com.lex.simplequest.presentation.base.BaseMvpPresenter
import com.lex.simplequest.presentation.utils.asRxSingle
import com.lex.simplequest.presentation.utils.tasks.SingleResultTask
import io.reactivex.android.schedulers.AndroidSchedulers

class MainActivityPresenter(
    //private val readSettingsInteractor: ReadSettingsInteractor,
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
    //private val taskReadSettings = createReadSettingsTask()
    private var locationTracker: LocationTracker? = null
    private var isTrackRecording: Boolean = false
    private var timePeriod: Long? = null

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
        val startResult = ui.startLocationTracker()
        if (null != startResult) {
            Log.i("qaz", "create(). started successfully")
        } else {
            Log.e("qaz", "create(). started failed")
        }
    }

    override fun resume() {
        val bond = ui.bindLocationTracker()
        if (bond) {
            Log.d("qaz", "resume(). bond successfully")
        } else {
            Log.w("qaz", "resume(). bond failed")
        }
        Log.d("qaz", "resume(). just start read settings")
//        taskReadSettings.start(ReadSettingsInteractor.Param(), Unit)
    }

    override fun pause() {
//        taskReadSettings.stop()
        isTrackRecording = locationTracker?.isRecording() ?: false
        Log.d("qaz", "pause(). unbind ")
        ui.unbindLocationTracker()
    }

    override fun destroy() {
        if (!isTrackRecording) {
            Log.e("qaz", "destroy(). track is not recording, destroy")
            ui.stopLocationTracker()
        } else {
            Log.w("qaz", "destroy(). track is Recording, keep it")
        }
    }

    override fun serviceConnected(locationTracker: LocationTracker) {
        Log.i("qaz", "Service connected")
        this.locationTracker = locationTracker
//        timePeriod?.let {
//            locationTracker.setLocationUpdateTimePeriod(it)
//        }
    }

    override fun serviceDisconnected() {
        Log.d("qaz", "Service disconnected")
        this.locationTracker = null
    }

    private fun  bindLocationTracker() {
        val bond = ui.bindLocationTracker()
        if (bond) {
            Log.d("qaz", "bond successfully")
        } else {
            Log.w("qaz", "bond failed")
        }
    }

//    private fun handleReadSettings(result: ReadSettingsInteractor.Result?, error: Throwable?) {
//        if (null != result) {
//            timePeriod = result.timePeriod
//        } else if (null != error) {
//            // show error
//        }
//
//        Log.d("qaz", "handleReadSettings called")
//        bindLocationTracker()
//    }
//
//    private fun createReadSettingsTask() =
//        SingleResultTask<ReadSettingsInteractor.Param, ReadSettingsInteractor.Result, Unit>(
//            TASK_READ_SETTINGS,
//            { param, _ ->
//                readSettingsInteractor.asRxSingle(param)
//                    .observeOn(AndroidSchedulers.mainThread())
//            },
//            { result, _ ->
//                handleReadSettings(result, null)
//
//            },
//            { error, _ ->
//                log.e(error, "Failed to read settings")
//                handleReadSettings(null, error)
//            }
//        )
}