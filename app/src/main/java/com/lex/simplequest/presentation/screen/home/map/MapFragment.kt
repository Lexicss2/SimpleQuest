package com.lex.simplequest.presentation.screen.home.map

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
import android.widget.ImageButton
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.lex.simplequest.App
import com.lex.simplequest.R
import com.lex.simplequest.device.service.TrackLocationService
import com.lex.simplequest.domain.locationmanager.LocationTracker
import com.lex.simplequest.domain.locationmanager.model.Location
import com.lex.simplequest.presentation.base.BaseMvpFragment
import com.lex.simplequest.presentation.screen.home.MainRouter
import com.softeq.android.mvp.PresenterStateHolder


class MapFragment :
    BaseMvpFragment<MapFragmentContract.Ui, MapFragmentContract.Presenter.State, MapFragmentContract.Presenter>(),
    MapFragmentContract.Ui, OnMapReadyCallback {
    companion object {
        fun newInstance(): MapFragment =
            MapFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    private var googleMap: GoogleMap? = null
    private var currentMarker: Marker? = null

    private lateinit var refreshButton: ImageButton

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TrackLocationService.TrackLocationBinder
            presenter.locationTrackerConnected(binder.getService() as LocationTracker)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            presenter.locationTrackerDisconnected()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        inflater.inflate(R.layout.fragment_map, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initGoogleMap()
        refreshButton = view.findViewById(R.id.refresh_button)
        refreshButton.setOnClickListener {
            presenter.refreshClicked()
        }
        super.onViewCreated(view, savedInstanceState)
    }

    // Draw path on Map
    //https://stackoverflow.com/questions/2176397/drawing-a-line-path-on-google-maps


    override fun onResume() {
        super.onResume()
        Intent(activity, TrackLocationService::class.java).also { intent ->
            val bond = activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onPause() {
        super.onPause()
        activity?.unbindService(connection)
        presenter.locationTrackerDisconnected() // Should be called because ServiceConnection.OnServiceDisconnected is not called
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.i("qaz", "MAP IS READY")
        this.googleMap = googleMap

        presenter.mapReady()
    }

    override fun showMarkerIfNeeded(location: Location) {
        googleMap?.let { map ->
            if (null == currentMarker) {
                Log.d("qaz", "showMarker $location")
                val markerOptions = MarkerOptions().position(LatLng(location.latitude, location.longitude))
                    .title("Im here").icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                currentMarker = map.addMarker(markerOptions)

                val builder = LatLngBounds.Builder()
                builder.include(markerOptions.position)
                val bounds = builder.build()
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 300, 300, 5)
                map.animateCamera(cameraUpdate)
            }
        }
    }

    override fun updateMarker(location: Location) {
        currentMarker?.remove()
        currentMarker = null
        showMarkerIfNeeded(location)
    }

    private fun initGoogleMap() {
        if (null == googleMap) {
            val googleMap = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
            if (null == googleMap) {
                Log.e("qaz", "Failed to Initialize GoogleMap")
                return
            }

            Log.i("qaz", "Map initialized ok!")
            googleMap.getMapAsync(this)
        }
    }

    override fun getUi(): MapFragmentContract.Ui =
        this

    override fun createPresenter(): MapFragmentContract.Presenter =
        MapFragmentPresenter(
            App.instance.internetConnectivityTracker,
            getTarget(MainRouter::class.java)!!
        )

    override fun createPresenterStateHolder(): PresenterStateHolder<MapFragmentContract.Presenter.State> =
        MapFragmentPresenterStateHolder()

}