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
import com.lex.simplequest.App
import com.lex.simplequest.R
import com.lex.simplequest.databinding.FragmentHomeBinding
import com.lex.simplequest.device.service.TrackLocationService
import com.lex.simplequest.domain.locationmanager.LocationTracker
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


    override fun onStart() {
        super.onStart()
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

    override fun onStop() {
        super.onStop()
    }

    override fun setButtonCaptionAsStart() {
        viewBinding.layoutContent.apply {
            startStopButton.text = resources.getString(R.string.start_tracking)
        }
    }

    override fun setButtonCaptionAsStop() {
        viewBinding.layoutContent.apply {
            startStopButton.text = resources.getString(R.string.stop_tracking)
        }
    }

    override fun getUi(): HomeFragmentContract.Ui =
        this

    override fun createPresenter(): HomeFragmentContract.Presenter =
        HomeFragmentPresenter(
            App.instance.internetConnectivityTracker,
            getTarget(MainRouter::class.java)!!
        )
    override fun createPresenterStateHolder(): PresenterStateHolder<HomeFragmentContract.Presenter.State> =
        HomeFragmentPresenterStateHolder()
}