package com.lex.simplequest.presentation.screen.home.settings

import com.lex.core.log.LogFactory
import com.lex.simplequest.domain.common.connectivity.InternetConnectivityTracker
import com.lex.simplequest.domain.repository.SettingsRepository
import com.lex.simplequest.domain.settings.interactor.ReadSettingsInteractor
import com.lex.simplequest.presentation.base.BaseMvpPresenter
import com.lex.simplequest.presentation.screen.home.MainRouter
import com.lex.simplequest.presentation.screen.home.home.HomeFragmentPresenter
import com.lex.simplequest.presentation.utils.asRxSingle
import com.lex.simplequest.presentation.utils.tasks.SingleResultTask
import io.reactivex.android.schedulers.AndroidSchedulers

class SettingsFragmentPresenter(
    private val readSettingsInteractor: ReadSettingsInteractor,
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
    }

    private val log = logFactory.get(TAG)
    private val taskReadSettings = createReadSettingsTask()
    private var timePeriod: Long? = null

    override fun start() {
        super.start()
        taskReadSettings.start(ReadSettingsInteractor.Param(), Unit)
        updateUi(0)
    }

    override fun stop() {
        super.stop()
        taskReadSettings.stop()
    }

    override fun accuracyClicked() {
        ui.showAccuracyPopup(timePeriod)
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
}