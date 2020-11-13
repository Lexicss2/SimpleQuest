package com.lex.simplequest.presentation.screen.home.settings

import com.lex.core.log.LogFactory
import com.lex.simplequest.domain.common.connectivity.InternetConnectivityTracker
import com.lex.simplequest.domain.repository.SettingsRepository
import com.lex.simplequest.domain.settings.interactor.ReadSettingsInteractor
import com.lex.simplequest.domain.settings.interactor.WriteSettingsInteractor
import com.lex.simplequest.presentation.base.BaseMvpPresenter
import com.lex.simplequest.presentation.screen.home.MainRouter
import com.lex.simplequest.presentation.screen.home.home.HomeFragmentPresenter
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
        private val PERIODS = arrayOf("1", "2", "5", "10", "30", "60", "120")
    }

    private val log = logFactory.get(TAG)
    private val taskReadSettings = createReadSettingsTask()
    private val taskWriteSettings = createWriteSettingsTask()
    private var timePeriod: Long? = null

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

    override fun accuracyClicked() {
        ui.showAccuracyPopup(timePeriod, PERIODS)
    }

    override fun selectedTimePeriod(timePeriodMs: Long) {
        this.timePeriod = timePeriodMs
        // set in repository
        if (!taskWriteSettings.isRunning()) {
            taskWriteSettings.start(WriteSettingsInteractor.Param(timePeriod), Unit)
        }
        // set in LocationManager
        // TODO: not finished
    }

    override fun reload() {

    }

    private fun updateUi(flags: Int) {
        if (isUiBinded) {
            ui.showProgress(taskReadSettings.isRunning())
            ui.showTimePeriod(timePeriod)
        }
    }

    private fun handleReadSettings(result: ReadSettingsInteractor.Result?, error: Throwable?) {
        if (null != result) {
            timePeriod = result.timePeriod
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