package com.lex.simplequest.presentation.screen.home.tracks.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.lex.simplequest.App
import com.lex.simplequest.R
import com.lex.simplequest.domain.locationmanager.model.Location
import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.domain.track.interactor.ReadTracksInteractorImpl
import com.lex.simplequest.presentation.base.BaseMvpFragment
import com.lex.simplequest.presentation.screen.home.MainRouter
import com.lex.simplequest.presentation.utils.canBeDrawn
import com.lex.simplequest.presentation.utils.screenSize
import com.lex.simplequest.presentation.utils.toLatLngBounds
import com.lex.simplequest.presentation.utils.toPolylineOptions
import com.softeq.android.mvp.PresenterStateHolder
import com.softeq.android.mvp.VoidPresenterStateHolder


class TrackViewFragment :
    BaseMvpFragment<TrackViewFragmentContract.Ui, TrackViewFragmentContract.Presenter.State, TrackViewFragmentContract.Presenter>(),
    TrackViewFragmentContract.Ui, OnMapReadyCallback {

    companion object {
        private const val ARG_TRACK_ID = "track_id"
        private const val BOUND_PADDING = 40
        private const val TRACK_WIDTH = 6.0f

        fun newInstance(trackId: Long): TrackViewFragment =
            TrackViewFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_TRACK_ID, trackId)
                }
            }
    }

    private var googleMap: GoogleMap? = null
    private var startMarker: Marker? = null
    private var finishMarker: Marker? = null
    private val currentPolyLines: MutableList<Polyline> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_track_view, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initGoogleMap()
        super.onViewCreated(view, savedInstanceState)
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
                startMarker = map.addMarker(markerOptions)
            }
        }
    }

    override fun showFinishMarker(location: Location?, shouldMoveCamera: Boolean) {
        googleMap?.let { map ->
            finishMarker?.remove()
            finishMarker = null

            if (null != location) {
                val title = resources.getString(
                    R.string.map_finish
                )
                val color = BitmapDescriptorFactory.HUE_RED
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

    override fun showTrack(track: Track?, shouldMoveCamera: Boolean) {
        googleMap?.let { map ->
            currentPolyLines.forEach { polyline ->
                polyline.remove()
            }
            currentPolyLines.clear()

            if (null != track) {
                val polylineOptionsList = track.toPolylineOptions(false, TRACK_WIDTH)
                val polyLines = polylineOptionsList.map { options ->
                    map.addPolyline(options)
                }

                if (shouldMoveCamera && polyLines.canBeDrawn()) {
                    val bounds = polyLines.toLatLngBounds()

                    val size = screenSize(requireActivity())
                    val width = size.width
                    val height = size.height
                    val cameraUpdate = CameraUpdateFactory.newLatLngBounds(
                        bounds,
                        width,
                        height,
                        BOUND_PADDING
                    )

                    map.moveCamera(cameraUpdate)
                }
                currentPolyLines.addAll(polyLines)
                toolbarInfo.setTitle(track.name)
            } else {
                toolbarInfo.setTitle(" ")
            }
        }
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

    override fun getUi(): TrackViewFragmentContract.Ui =
        this

    override fun createPresenter(): TrackViewFragmentContract.Presenter =
        TrackViewFragmentPresenter(
            arguments!!.getLong(ARG_TRACK_ID),
            ReadTracksInteractorImpl(App.instance.locationRepository),
            App.instance.internetConnectivityTracker,
            App.instance.logFactory,
            getTarget(MainRouter::class.java)!!
        )

    override fun createPresenterStateHolder(): PresenterStateHolder<TrackViewFragmentContract.Presenter.State> =
        VoidPresenterStateHolder()
}