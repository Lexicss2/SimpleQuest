package com.lex.simplequest.presentation.screen.home.home

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.common.ConnectionResult
import com.lex.simplequest.App
import com.lex.simplequest.R
import com.lex.simplequest.databinding.FragmentHomeBinding
import com.lex.simplequest.device.service.TrackLocationService
import com.lex.simplequest.domain.locationmanager.LocationTracker
import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.domain.model.distance
import com.lex.simplequest.domain.track.interactor.ReadTracksInteractorImpl
import com.lex.simplequest.presentation.base.BaseMvpLceFragment
import com.lex.simplequest.presentation.screen.home.MainRouter
import com.softeq.android.mvp.PresenterStateHolder

class HomeFragment :
    BaseMvpLceFragment<HomeFragmentContract.Ui, HomeFragmentContract.Presenter.State, HomeFragmentContract.Presenter>(),
    HomeFragmentContract.Ui {

    companion object {
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
            Log.i("qaz", "on service Connected")
            val binder = service as TrackLocationService.TrackLocationBinder
            presenter.locationTrackerConnected(binder.getService() as LocationTracker)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.e("qaz", "on service disconnected")
            presenter.locationTrackerDisconnected()
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
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        Log.d("qaz", "Fragment onResume, bind to Service")
        Intent(activity, TrackLocationService::class.java).also { intent ->
            val bond = activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            Log.d("qaz", "bond = $bond")
        }
    }

    override fun onPause() {
        super.onPause()
        Log.w("qaz", "Fragment onPause, unbind to Service")
        activity?.unbindService(connection)
        presenter.locationTrackerDisconnected() // Should be called because ServiceConnection.OnServiceDisconnected is not called
    }

    override fun setButtonStyleRecording(recordButtonType: RecordButtonType) {
        viewBinding.layoutContent.apply {
            when (recordButtonType) {
                RecordButtonType.STOPPED -> {
                    startStopButton.apply {
                        text = getString(R.string.home_start_tracking)
                        setBackgroundColor(
                            resources.getColor(
                                R.color.colorBgStartButton,
                                null
                            )
                        )
                        isEnabled = true
                    }
                }

                RecordButtonType.GOING_TO_RECORD -> {
                    startStopButton.apply {
                        text = getString(R.string.home_start_tracking)
                        setBackgroundColor(
                            resources.getColor(
                                R.color.colorBgGray,
                                null
                            )
                        )
                        isEnabled = false
                    }
                }

                RecordButtonType.RECORDING -> {
                    startStopButton.apply {
                        text = getString(R.string.home_stop_tracking)
                        setBackgroundColor(
                            resources.getColor(
                                R.color.colorBgStopButton,
                                null
                            )
                        )
                        isEnabled = true
                    }
                }
            }
        }
    }

    override fun showLastTrackInfo(track: Track?, isRecording: Boolean) {
        viewBinding.layoutContent.apply {
            if (track != null) {
                lastTrackNameView.text = track.name
                lastTrackDistanceView.text = String.format("%.2f m", track.distance())
            } else {
                lastTrackNameView.text = resources.getString(R.string.home_no_tracks)
                lastTrackDistanceView.text = "---"
            }

            lastTrackCaption.text =
                if (isRecording) resources.getString(R.string.home_is_recording) else resources.getString(
                    R.string.home_last_track_name
                )
        }
    }

    override fun showProgress(show: Boolean) {
        viewBinding.apply {
            layoutContent.root.visibility = if (show) View.GONE else View.VISIBLE
            layoutLoading.root.visibility = if (show) View.VISIBLE else View.GONE
        }
    }

    override fun setDurationMinutesSeconds(minutes: String, seconds: String) {
        viewBinding.layoutContent.apply {
            minutesDurationTextView.text = minutes
            secondsDurationTextView.text = seconds
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

    override fun setTrackerStatus(status: LocationTracker.Status?) {
        viewBinding.layoutContent.trackerStatusTextView.text = status.toString()
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