package com.lex.simplequest.presentation.screen.home.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lex.simplequest.App
import com.lex.simplequest.R
import com.lex.simplequest.databinding.FragmentSettingsBinding
import com.lex.simplequest.domain.settings.interactor.ReadSettingsInteractorImpl
import com.lex.simplequest.domain.settings.interactor.WriteSettingsInteractorImpl
import com.lex.simplequest.presentation.base.BaseMvpLceFragment
import com.lex.simplequest.presentation.screen.home.MainActivity
import com.lex.simplequest.presentation.screen.home.MainRouter
import com.lex.simplequest.presentation.utils.isDialogShown
import com.lex.simplequest.presentation.utils.showDialog
import com.softeq.android.mvp.PresenterStateHolder
import com.softeq.android.mvp.VoidPresenterStateHolder

class SettingsFragment :
    BaseMvpLceFragment<SettingsFragmentContract.Ui, SettingsFragmentContract.Presenter.State, SettingsFragmentContract.Presenter>(),
    SettingsFragmentContract.Ui, SelectGpsAccuracyDialog.OnTimePeriodSelectedListener,
    SelectTrackSensitivityDialog.OnDistanceSelectedListener,
    SelectMinimalDisplacementDialog.OnMinimalDisplacementSelectedListener,
    SelectBatteryLevelDialog.OnBatteryLevelSelectedListener {
    companion object {
        private const val DLG_SELECT_GPS_ACCURACY = "select_gps_accuracy"
        private const val DLG_SELECT_TRACK_SENSITIVITY = "select_track_sensitivity"
        private const val DLG_SELECT_DISPLACEMENT = "select_displacement"
        private const val DLG_SELECT_BATTERY_LEVEL = "select_battery_level"
        private const val DLG_ABOUT = "about"

        fun newInstance(): SettingsFragment =
            SettingsFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    private var _viewBinding: FragmentSettingsBinding? = null
    private val viewBinding: FragmentSettingsBinding
        get() = _viewBinding!!

    private val bottomBarHeightChangeListener = object : MainActivity.BottomBarHeightListener {
        override fun onBottomBarHeightChanged(height: Int) {
            if (viewBinding.layoutContent.scrollView.layoutParams is ViewGroup.MarginLayoutParams) {
                val lp =
                    viewBinding.layoutContent.scrollView.layoutParams as ViewGroup.MarginLayoutParams
                if (lp.bottomMargin != height) {
                    lp.bottomMargin = height
                    viewBinding.layoutContent.scrollView.layoutParams = lp
                }
            }
        }
    }

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
            displacementLayout.setOnClickListener {
                presenter.displacementClicked()
            }
            batteryLevelLayout.setOnClickListener {
                presenter.batteryLevelClicked()
            }
            aboutLayout.setOnClickListener {
                presenter.aboutClicked()
            }
        }
        toolbarInfo.setTitle(resources.getString(R.string.settings_title))
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        if (activity is MainActivity) {
            (activity as MainActivity).bottomBarHeightListener = bottomBarHeightChangeListener
        }
    }

    override fun onPause() {
        super.onPause()
        if (activity is MainActivity) {
            (activity as MainActivity).bottomBarHeightListener = null
        }
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

    override fun showDisplacement(displacement: Long?) {
        viewBinding.layoutContent.apply {
            if (null != displacement) {
                displacementTextView.text =
                    String.format(
                        resources.getString(R.string.settings_minimal_displacement_value),
                        displacement
                    )
            }
        }
    }

    override fun showBatteryLevel(batteryLevel: Int?) {
        viewBinding.layoutContent.apply {
            if (null != batteryLevel) {
                batteryLevelTextView.text =
                    String.format(
                        resources.getString(R.string.settings_minimal_battery_level_value),
                        batteryLevel
                    )
            }
        }
    }

    override fun onTimePeriodSelected(timePeriodMs: Long) {
        presenter.selectedTimePeriod(timePeriodMs)
    }

    override fun onDistanceSelected(distanceM: Long) {
        presenter.selectDistance(distanceM)
    }

    override fun onMinimalDisplacementSelected(minimalDisplacement: Long) {
        presenter.selectDisplacement(minimalDisplacement)
    }

    override fun onBatteryLevelSelected(batteryLevelPc: Int) {
        presenter.selectBatteryLevel(batteryLevelPc)
    }

    override fun showGpsAccuracyPopup(timePeriodMs: Long?, availablePeriods: Array<String>) {
        if (!childFragmentManager.isDialogShown(DLG_SELECT_GPS_ACCURACY)) {
            val dlg = SelectGpsAccuracyDialog.newInstance(timePeriodMs, availablePeriods)
            childFragmentManager.showDialog(dlg, DLG_SELECT_GPS_ACCURACY)
        }
    }

    override fun showTrackSensitivityPopup(distance: Long?, availableDistances: Array<String>) {
        if (!childFragmentManager.isDialogShown(DLG_SELECT_TRACK_SENSITIVITY)) {
            val dlg = SelectTrackSensitivityDialog.newInstance(distance, availableDistances)
            childFragmentManager.showDialog(dlg, DLG_SELECT_TRACK_SENSITIVITY)
        }
    }

    override fun showDisplacementPopup(displacement: Long?, availableDisplacements: Array<String>) {
        if (!childFragmentManager.isDialogShown(DLG_SELECT_DISPLACEMENT)) {
            val dlg =
                SelectMinimalDisplacementDialog.newInstance(displacement, availableDisplacements)
            childFragmentManager.showDialog(dlg, DLG_SELECT_DISPLACEMENT)
        }
    }

    override fun showBatteryLevelPopup(batteryLevel: Int?, availableBatteryLevels: Array<String>) {
        if (!childFragmentManager.isDialogShown(DLG_SELECT_BATTERY_LEVEL)) {
            val dlg = SelectBatteryLevelDialog.newInstance(batteryLevel, availableBatteryLevels)
            childFragmentManager.showDialog(dlg, DLG_SELECT_BATTERY_LEVEL)
        }
    }

    override fun showAboutPopup() {
        if (!childFragmentManager.isDialogShown(DLG_ABOUT)) {
            val dlg = AboutDialog.newInstance()
            childFragmentManager.showDialog(dlg, DLG_ABOUT)
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