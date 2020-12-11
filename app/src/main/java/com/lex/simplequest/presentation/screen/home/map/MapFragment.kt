package com.lex.simplequest.presentation.screen.home.map

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
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
import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.domain.track.interactor.ReadTracksInteractorImpl
import com.lex.simplequest.presentation.base.BaseMvpFragment
import com.lex.simplequest.presentation.screen.home.MainRouter
import com.lex.simplequest.presentation.utils.canBeDrawn
import com.lex.simplequest.presentation.utils.toLatLngBounds
import com.lex.simplequest.presentation.utils.toPolylineOptions
import com.softeq.android.mvp.PresenterStateHolder

class MapFragment :
    BaseMvpFragment<MapFragmentContract.Ui, MapFragmentContract.Presenter.State, MapFragmentContract.Presenter>(),
    MapFragmentContract.Ui, OnMapReadyCallback {
    companion object {
        private const val BOUND_WIDTH = 300
        private const val BOUND_HEIGHT = 300
        private const val BOUND_PADDING = 5
        private const val TRACK_WIDTH = 6.0f

        fun newInstance(): MapFragment =
            MapFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    private var googleMap: GoogleMap? = null
    private lateinit var refreshButton: Button
    private lateinit var trackNameTextView: TextView
    private lateinit var indicatorTextView: TextView
    private var startMarker: Marker? = null
    private var finishMarker: Marker? = null
    private val currentPolylines: MutableList<Polyline> = mutableListOf()

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
    ): View =
        inflater.inflate(R.layout.fragment_map, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initGoogleMap()
        refreshButton = view.findViewById(R.id.refresh_button)
        refreshButton.setOnClickListener {
            presenter.refreshClicked()
        }
        trackNameTextView = view.findViewById(R.id.track_name_text_view)
        indicatorTextView = view.findViewById(R.id.indicator_text_view)
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

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        presenter.mapReady()
    }

    override fun showStartMarker(location: Location?) {
        googleMap?.let { map ->
            startMarker?.remove()
            startMarker = null

            if (null != location) {
                val markerOptions = MarkerOptions()
                    .position(LatLng(location.latitude, location.longitude))
                    .title(resources.getString(R.string.map_start))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                val marker = map.addMarker(markerOptions)
                // No need to move camera
                startMarker = marker
            }
        }
    }

    override fun showFinishMarker(location: Location?, isRecording: Boolean, shouldMoveCamera: Boolean) {
        googleMap?.let { map ->
            finishMarker?.remove()
            finishMarker = null

            if (null != location) {
                val title =
                    if (isRecording) resources.getString(R.string.map_iam_here) else resources.getString(
                        R.string.map_finish
                    )
                val color =
                    if (isRecording) BitmapDescriptorFactory.HUE_AZURE else BitmapDescriptorFactory.HUE_RED
                val markerOptions = MarkerOptions()
                    .position(LatLng(location.latitude, location.longitude))
                    .title(title)
                    .icon(BitmapDescriptorFactory.defaultMarker(color))
                val marker = map.addMarker(markerOptions)

                if (shouldMoveCamera) {
                    val cameraUpdate =
                        CameraUpdateFactory.newLatLng(marker.position)
                    map.moveCamera(cameraUpdate)
                }

                finishMarker = marker
            }
        }
    }


    override fun showTrack(track: Track?, isRecording: Boolean, shouldMoveCamera: Boolean) {
        googleMap?.let { map ->
            currentPolylines.forEach { polyline ->
                polyline.remove()
            }
            currentPolylines.clear()

            if (null != track) {
                val polylineOptionsList = track.toPolylineOptions(isRecording, TRACK_WIDTH)
                val polyLines = polylineOptionsList.map { options ->
                    map.addPolyline(options)
                }

                if (shouldMoveCamera && polyLines.canBeDrawn()) {
                    val bounds = polyLines.toLatLngBounds()
                    val cameraUpdate = CameraUpdateFactory.newLatLngBounds(
                        bounds,
                        BOUND_WIDTH,
                        BOUND_HEIGHT,
                        BOUND_PADDING
                    )
                    map.moveCamera(cameraUpdate)
                }
                currentPolylines.addAll(polyLines)

                trackNameTextView.apply {
                    if (isRecording) {
                        text = String.format(resources.getString(R.string.map_recording_track), track.name)
                        setTextColor(resources.getColor(R.color.colorSimpleRed, null))
                    } else {
                        text = track.name
                        setTextColor(resources.getColor(R.color.colorSimpleBlack, null))
                    }
                    visibility = View.VISIBLE
                }
                trackNameTextView.visibility = View.VISIBLE
            } else {
                trackNameTextView.visibility = View.GONE
            }
        }
    }

    override fun showIndicatorProgress(text: String) {
        indicatorTextView.text = text
    }

    override fun showError(error: Throwable) {
        Toast.makeText(activity, error.localizedMessage, Toast.LENGTH_SHORT).show()
    }

    private fun initGoogleMap() {
        if (null == googleMap) {
            val googleMap = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
            googleMap?.getMapAsync(this)
        }
    }

    override fun getUi(): MapFragmentContract.Ui =
        this

    override fun createPresenter(): MapFragmentContract.Presenter =
        MapFragmentPresenter(
            ReadTracksInteractorImpl(App.instance.locationRepository),
            App.instance.internetConnectivityTracker,
            App.instance.logFactory,
            getTarget(MainRouter::class.java)!!
        )

    override fun createPresenterStateHolder(): PresenterStateHolder<MapFragmentContract.Presenter.State> =
        MapFragmentPresenterStateHolder()

}