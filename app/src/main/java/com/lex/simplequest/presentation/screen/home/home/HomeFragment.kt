package com.lex.simplequest.presentation.screen.home.home

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Typeface
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.common.ConnectionResult
import com.lex.simplequest.App
import com.lex.simplequest.R
import com.lex.simplequest.databinding.FragmentHomeBinding
import com.lex.simplequest.device.service.TrackLocationService
import com.lex.simplequest.domain.locationmanager.LocationTracker
import com.lex.simplequest.domain.track.interactor.ReadTracksInteractorImpl
import com.lex.simplequest.presentation.base.BaseMvpLceFragment
import com.lex.simplequest.presentation.screen.home.MainRouter
import com.softeq.android.mvp.PresenterStateHolder

class HomeFragment :
    BaseMvpLceFragment<HomeFragmentContract.Ui, HomeFragmentContract.Presenter.State, HomeFragmentContract.Presenter>(),
    HomeFragmentContract.Ui {

    companion object {
        private const val NO_DATA = "--"

        fun newInstance(): HomeFragment =
            HomeFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    private var _viewBinding: FragmentHomeBinding? = null
    private val viewBinding: FragmentHomeBinding
        get() = _viewBinding!!

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TrackLocationService.TrackLocationBinder
            presenter.locationTrackerServiceConnected(binder.getService() as LocationTracker)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            presenter.locationTrackerServiceDisconnected()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentHomeBinding.inflate(inflater, container, false)
        .also { _viewBinding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewBinding.layoutContent.apply {
            startStopButton.setOnClickListener {
                presenter.startStopClicked()
            }
            pauseResumeButton.setOnClickListener {
                presenter.pauseResumeClicked()
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        Intent(activity, TrackLocationService::class.java).also { intent ->
            activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onPause() {
        super.onPause()
        activity?.unbindService(connection)
        presenter.locationTrackerServiceDisconnected() // Should be called because ServiceConnection.OnServiceDisconnected is not called
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _viewBinding = null
    }

    override fun setButtonStyleRecording(recordingStatus: RecordingStatus?) {
        viewBinding.layoutContent.apply {
            when (recordingStatus) {
                RecordingStatus.STOPPED -> {
                    startStopButton.apply {
                        text = getString(R.string.home_start_tracking)
                        setTextColor(resources.getColor(R.color.colorBgStartButton, null))
                        background = ResourcesCompat.getDrawable(resources, R.drawable.shape_button_start, null)
                        isEnabled = true
                    }
                    pauseResumeButton.visibility = View.GONE
                    delimiterView.visibility = View.GONE
                }

                RecordingStatus.GOING_TO_RECORD -> {
                    startStopButton.apply {
                        text = getString(R.string.home_start_tracking)
                        setTextColor(resources.getColor(R.color.colorBgGray, null))
                        background = ResourcesCompat.getDrawable(resources, R.drawable.shape_button_disabled, null)
                        isEnabled = false
                    }
                    pauseResumeButton.visibility = View.GONE
                    delimiterView.visibility = View.GONE
                }

                RecordingStatus.RECORDING -> {
                    startStopButton.apply {
                        text = getString(R.string.home_stop_tracking)
                        setTextColor(resources.getColor(R.color.colorBgStopButton, null))
                        background = ResourcesCompat.getDrawable(resources, R.drawable.shape_button_stop, null)
                        isEnabled = true
                    }
                    pauseResumeButton.visibility = View.VISIBLE
                    pauseResumeButton.apply {
                        text = resources.getString(R.string.home_pause_tracking)
                        setTextColor(resources.getColor(R.color.colorBgPauseButton, null))
                        background = ResourcesCompat.getDrawable(resources, R.drawable.shape_button_pause, null)
                    }
                    delimiterView.visibility = View.VISIBLE
                }

                RecordingStatus.PAUSED -> {
                    startStopButton.apply {
                        text = getString(R.string.home_stop_tracking)
                        setTextColor(resources.getColor(R.color.colorBgStopButton, null))
                        background = ResourcesCompat.getDrawable(resources, R.drawable.shape_button_stop, null)
                        isEnabled = true
                    }
                    pauseResumeButton.visibility = View.VISIBLE
                    pauseResumeButton.apply {
                        text = resources.getString(R.string.home_resume_tracking)
                        setTextColor(resources.getColor(R.color.colorBgResumeButton, null))
                        background = ResourcesCompat.getDrawable(resources, R.drawable.shape_button_resume, null)
                    }
                    delimiterView.visibility = View.VISIBLE
                }

                null -> {
                    startStopButton.apply {
                        setBackgroundColor(
                            resources.getColor(
                                R.color.colorBgGray,
                                null
                            )
                        )
                        isEnabled = false
                    }
                    pauseResumeButton.visibility = View.GONE
                    delimiterView.visibility = View.GONE
                }
            }
        }
    }

    override fun showLastTrackName(trackName: String?, recordingStatus: RecordingStatus?) {
        val caption = trackName ?: resources.getString(R.string.home_no_tracks)
        viewBinding.layoutContent.apply {
            lastTrackNameView.text = caption
            lastTrackCaption.apply {
                when(recordingStatus) {
                    RecordingStatus.RECORDING -> {
                        text = resources.getString(R.string.home_is_recording)
                        setTextColor(resources.getColor(R.color.colorSimpleRed, null))
                    }
                    RecordingStatus.PAUSED -> {
                        text = resources.getString(R.string.home_is_recording_paused)
                        setTextColor(resources.getColor(R.color.colorSimpleYellow, null))
                    }
                    else -> {
                        text = resources.getString(R.string.home_last_track_name)
                        setTextColor(resources.getColor(R.color.colorPrimary, null))
                    }
                }
            }
        }
        toolbarInfo.setTitle(caption)
    }

    override fun showProgress(show: Boolean) {
        viewBinding.apply {
            layoutContent.root.visibility = if (show) View.GONE else View.VISIBLE
            layoutLoading.root.visibility = if (show) View.VISIBLE else View.GONE
        }
    }

    override fun showLastTrackDuration(minutes: String?, seconds: String?) {
        viewBinding.layoutContent.apply {
            minutesDurationTextView.text = minutes ?: NO_DATA
            secondsDurationTextView.text = seconds ?: NO_DATA
        }
    }

    override fun showLastTrackDistance(distance: String?, withBoldStyle: Boolean) {
        viewBinding.layoutContent.lastTrackDistanceView.apply {
            setTypeface(typeface, if (withBoldStyle) Typeface.BOLD else Typeface.NORMAL)
            text = distance ?: NO_DATA
        }
    }

    override fun setLocationAvailableStatus(isAvailable: Boolean?) {
        viewBinding.layoutContent.apply {
            if (null != isAvailable) {
                locationAvailabilityStatusTextView.visibility = View.VISIBLE
                if (isAvailable) {
                    locationAvailabilityStatusTextView.apply {
                        setTextColor(resources.getColor(R.color.colorSuccess, null))
                        text = resources.getString(R.string.home_location_is_available)
                    }
                } else {
                    locationAvailabilityStatusTextView.apply {
                        setTextColor(resources.getColor(R.color.colorWarning, null))
                        text = resources.getString(R.string.home_location_is_not_available)
                    }
                }
            } else {
                locationAvailabilityStatusTextView.visibility = View.GONE
            }
        }
    }

    override fun setLocationSuspendedStatus(reason: Int?) {
        viewBinding.layoutContent.apply {
            if (null != reason) {
                locationSuspendedStatusTextView.visibility = View.VISIBLE
                if (ConnectionResult.SUCCESS == reason) {
                    locationSuspendedStatusTextView.apply {
                        setTextColor(resources.getColor(R.color.colorSuccess, null))
                        text = String.format(
                            resources.getString(R.string.home_location_suspended_status),
                            reason
                        )
                    }
                } else {
                    locationSuspendedStatusTextView.apply {
                        setTextColor(resources.getColor(R.color.colorError, null))
                        text = String.format(
                            resources.getString(R.string.home_location_suspended_status),
                            reason
                        )
                    }
                }
            } else {
                locationSuspendedStatusTextView.visibility = View.GONE
            }
        }
    }

    override fun setError(error: Throwable?) {
        viewBinding.layoutContent.apply {
            if (null != error) {
                errorStatusTextView.visibility = View.VISIBLE
                errorStatusTextView.text = error.localizedMessage
            } else {
                errorStatusTextView.visibility = View.GONE
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun setTrackerStatus(status: LocationTracker.Status?, tag: String?) {
        viewBinding.layoutContent.trackerStatusTextView.text = status.toString() + " " + tag
    }

    override fun getUi(): HomeFragmentContract.Ui =
        this

    override fun createPresenter(): HomeFragmentContract.Presenter =
        HomeFragmentPresenter(
            ReadTracksInteractorImpl(App.instance.locationRepository),
            App.instance.internetConnectivityTracker,
            App.instance.logFactory,
            getTarget(MainRouter::class.java)!!
        )

    override fun createPresenterStateHolder(): PresenterStateHolder<HomeFragmentContract.Presenter.State> =
        HomeFragmentPresenterStateHolder()
}