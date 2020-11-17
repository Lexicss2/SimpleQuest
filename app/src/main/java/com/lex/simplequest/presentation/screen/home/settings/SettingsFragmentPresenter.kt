package com.lex.simplequest.presentation.screen.home.settings

import com.lex.core.log.LogFactory
import com.lex.simplequest.Config
import com.lex.simplequest.domain.common.connectivity.InternetConnectivityTracker
import com.lex.simplequest.domain.settings.interactor.ReadSettingsInteractor
import com.lex.simplequest.domain.settings.interactor.WriteSettingsInteractor
import com.lex.simplequest.presentation.base.BaseMvpPresenter
import com.lex.simplequest.presentation.screen.home.MainRouter
import com.lex.simplequest.presentation.utils.asRxSingle
import com.lex.simplequest.presentation.utils.tasks.SingleResultTask
import io.reactivex.android.schedulers.AndroidSchedulers

class SettingsFragmentPresenter(
    private val readSettingsInteractor: ReadSettingsInteractor,
    private val writeSettingsInteractor: WriteSettingsInteractor,
    internetConnectivityTracker: InternetConnectivityTracker,
    logFactory: LogFactory,
    router: MainRouter
) : BaseMvpPresenter<SettingsFragmentContract.Ui, SettingsFragmentContract.Presenter.State, MainRouter>(
    router
),
    SettingsFragmentContract.Presenter {

    companion object {
        private const val TAG = "SettingsFragmentPresenter"
        private const val TASK_READ_SETTINGS = "task_read_settings"
        private const val TASK_WRITE_SETTINGS = "task_write_settings"
    }

    private val log = logFactory.get(TAG)
    private val taskReadSettings = createReadSettingsTask()
    private val taskWriteSettings = createWriteSettingsTask()
    private var timePeriod: Long? = null
    private var distance: Long? = null
    private var displacement: Long? = null
    private var batteryLevel: Int? = null

    override fun start() {
        super.start()
        taskReadSettings.start(ReadSettingsInteractor.Param(), Unit)
        updateUi(0)
    }

    override fun stop() {
        super.stop()
        taskReadSettings.stop()
        taskWriteSettings.stop()
    }

    override fun gpsAccuracyClicked() {
        ui.showGpsAccuracyPopup(timePeriod, Config.AVAILABLE_GPS_ACCURACY_TIME_PERIODS_S)
    }

    override fun trackSensitivityClicked() {
        ui.showTrackSensitivityPopup(distance, Config.AVAILABLE_TRACK_DISTANCES_M)
    }

    override fun displacementClicked() {
        ui.showDisplacementPopup(displacement, Config.AVAILABLE_DISPLACEMENTS_M)
    }

    override fun batteryLevelClicked() {
        ui.showBatteryLevelPopup(batteryLevel, Config.AVAILABLE_BATTERY_LEVELS)
    }

    override fun aboutClicked() {
        ui.showAboutPopup()
    }

    override fun selectedTimePeriod(timePeriodMs: Long) {
        this.timePeriod = timePeriodMs
        if (!taskWriteSettings.isRunning()) {
            taskWriteSettings.start(WriteSettingsInteractor.Param(timePeriod, null, null, null), Unit)
        }
        updateUi(0)
    }

    override fun selectDistance(distance: Long) {
        this.distance = distance
        if (!taskWriteSettings.isRunning()) {
            taskWriteSettings.start(WriteSettingsInteractor.Param(null, distance, null, null), Unit)
        }
        updateUi(0)
    }

    override fun selectDisplacement(displacement: Long) {
        this.displacement = displacement
        if (!taskWriteSettings.isRunning()) {
            taskWriteSettings.start(WriteSettingsInteractor.Param(null, null, displacement, null), Unit)
        }
        updateUi(0)
    }

    override fun selectBatteryLevel(batteryLevel: Int) {
        this.batteryLevel = batteryLevel
        if (!taskWriteSettings.isRunning()) {
            taskWriteSettings.start(WriteSettingsInteractor.Param(null, null, null, batteryLevel), Unit)
        }
    }

    override fun reload() {

    }

    private fun updateUi(flags: Int) {
        if (isUiBinded) {
            ui.showProgress(taskReadSettings.isRunning())
            ui.showTimePeriod(timePeriod)
            ui.showDistance(distance)
            ui.showDisplacement(displacement)
            ui.showBatteryLevel(batteryLevel)
        }
    }

    private fun handleReadSettings(result: ReadSettingsInteractor.Result?, error: Throwable?) {
        if (null != result) {
            timePeriod = result.timePeriod
            distance = result.distance
            displacement = result.displacement
            batteryLevel = result.batteryLevel
        } else if (null != error) {
            // show error
        }
        updateUi(0)
    }

    private fun createReadSettingsTask() =
        SingleResultTask<ReadSettingsInteractor.Param, ReadSettingsInteractor.Result, Unit>(
            TASK_READ_SETTINGS,
            { param, _ ->
                readSettingsInteractor.asRxSingle(param)
                    .observeOn(AndroidSchedulers.mainThread())
            },
            { result, _ ->
                handleReadSettings(result, null)

            },
            { error, _ ->
                log.e(error, "Failed to read settings")
                handleReadSettings(null, error)
            }
        )

    private fun handleWriteSettings(error: Throwable?) {
        if (null == error) {
            if (!taskReadSettings.isRunning()) {
                taskReadSettings.start(ReadSettingsInteractor.Param(), Unit)
            }
        } else {
            // Handle error
        }
    }

    private fun createWriteSettingsTask() =
        SingleResultTask<WriteSettingsInteractor.Param, WriteSettingsInteractor.Result, Unit>(
            TASK_WRITE_SETTINGS,
            { param, _ ->
                writeSettingsInteractor.asRxSingle(param)
                    .observeOn(AndroidSchedulers.mainThread())
            },
            { _, _ ->
                handleWriteSettings(null)
            },
            { error, _ ->
                log.e(error, "Failed to write settings")
                handleWriteSettings(error)
            }
        )
}