package com.lex.simplequest.presentation.screen.home.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lex.simplequest.App
import com.lex.simplequest.R
import com.lex.simplequest.databinding.FragmentSettingsBinding
import com.lex.simplequest.domain.settings.interactor.ReadSettingsInteractorImpl
import com.lex.simplequest.domain.settings.interactor.WriteSettingsInteractorImpl
import com.lex.simplequest.presentation.base.BaseMvpLceFragment
import com.lex.simplequest.presentation.screen.home.MainRouter
import com.lex.simplequest.presentation.utils.isDialogShown
import com.lex.simplequest.presentation.utils.showDialog
import com.softeq.android.mvp.PresenterStateHolder
import com.softeq.android.mvp.VoidPresenterStateHolder

class SettingsFragment :
    BaseMvpLceFragment<SettingsFragmentContract.Ui, SettingsFragmentContract.Presenter.State, SettingsFragmentContract.Presenter>(),
    SettingsFragmentContract.Ui, SelectGpsAccuracyDialog.OnTimePeriodSelectedListener, SelectTrackSensitivityDialog.OnDistanceSelectedListener {
    companion object {
        private const val DLG_SELECT_GPS_ACCURACY = "select_gps_accuracy"
        private const val DLG_SELECT_TRACK_SENSITIVITY = "select_track_sensitivity"

        fun newInstance(): SettingsFragment =
            SettingsFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    private var _viewBinding: FragmentSettingsBinding? = null
    private val viewBinding: FragmentSettingsBinding
        get() = _viewBinding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentSettingsBinding.inflate(inflater, container, false)
        .also { _viewBinding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewBinding.layoutContent.apply {
            gpsAccuracyLayout.setOnClickListener {
                presenter.gpsAccuracyClicked()
            }
            trackSensitivityLayout.setOnClickListener {
                presenter.trackSensitivityClicked()
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _viewBinding = null
    }

    override fun showProgress(show: Boolean) {
        viewBinding.apply {
            if (show) {
                layoutContent.root.visibility = View.GONE
                layoutLoading.root.visibility = View.VISIBLE
            } else {
                layoutContent.root.visibility = View.VISIBLE
                layoutLoading.root.visibility = View.GONE
            }
        }
    }

    override fun showTimePeriod(timePeriodMs: Long?) {
        viewBinding.layoutContent.apply {
            if (null != timePeriodMs) {
                val seconds = (timePeriodMs / 1000L).toInt()
                timePeriodTextView.text =
                    String.format(
                        resources.getString(R.string.settings_gps_accuracy_value),
                        seconds
                    )
            }
        }
    }

    override fun showDistance(distance: Long?) {
        viewBinding.layoutContent.apply {
            if (null != distance) {
                trackSensitivityTextView.text =
                    String.format(
                        resources.getString(R.string.settings_track_record_sensitivity_value),
                        distance
                    )
            }
        }
    }

    override fun showGpsAccuracyPopup(timePeriodMs: Long?, periods: Array<String>) {
        if (!childFragmentManager.isDialogShown(DLG_SELECT_GPS_ACCURACY)) {
            val dlg = SelectGpsAccuracyDialog.newInstance(timePeriodMs, periods).apply {
                //setTargetFragment(this@SettingsFragment, 1)
            }

            childFragmentManager.showDialog(dlg, DLG_SELECT_GPS_ACCURACY)
        }
    }

    override fun onTimePeriodSelected(timePeriodMs: Long) {
        Log.d("qaz", "onTimePeriodSelected: $timePeriodMs")
        presenter.selectedTimePeriod(timePeriodMs)
    }

    override fun onDistanceSelected(distanceM: Long) {
        presenter.selectDistance(distanceM)
    }

    override fun showTrackSensitivityPopup(distance: Long?, distances: Array<String>) {
        if (!childFragmentManager.isDialogShown(DLG_SELECT_TRACK_SENSITIVITY)) {
            val dlg = SelectTrackSensitivityDialog.newInstance(distance, distances)
            childFragmentManager.showDialog(dlg, DLG_SELECT_TRACK_SENSITIVITY)
        }
    }

    override fun getUi(): SettingsFragmentContract.Ui =
        this

    override fun createPresenter(): SettingsFragmentContract.Presenter =
        SettingsFragmentPresenter(
            ReadSettingsInteractorImpl(App.instance.settingsRepository),
            WriteSettingsInteractorImpl(App.instance.settingsRepository),
            App.instance.internetConnectivityTracker,
            App.instance.logFactory,
            getTarget(MainRouter::class.java)!!
        )

    override fun createPresenterStateHolder(): PresenterStateHolder<SettingsFragmentContract.Presenter.State> =
        VoidPresenterStateHolder()

}