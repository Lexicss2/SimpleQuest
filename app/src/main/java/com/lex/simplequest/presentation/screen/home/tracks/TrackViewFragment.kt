package com.lex.simplequest.presentation.screen.home.tracks

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.lex.simplequest.App
import com.lex.simplequest.R
import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.domain.model.toLatLngs
import com.lex.simplequest.domain.track.interactor.ReadTracksInteractorImpl
import com.lex.simplequest.presentation.base.BaseMvpFragment
import com.lex.simplequest.presentation.screen.home.MainRouter
import com.softeq.android.mvp.PresenterStateHolder
import com.softeq.android.mvp.VoidPresenterStateHolder
import kotlinx.android.synthetic.main.fragment_map.*

class TrackViewFragment :
    BaseMvpFragment<TrackViewFragmentContract.Ui, TrackViewFragmentContract.Presenter.State, TrackViewFragmentContract.Presenter>(),
    TrackViewFragmentContract.Ui, OnMapReadyCallback {

    companion object {
        private const val ARG_TRACK_ID = "track_id"
        private const val INITIAL_ZOOM = 12.0f
        private const val TARGET_ZOOM = 18.0f
        private const val ZOOM_DURATION_MS = 2000
        private const val BOUND_WIDTH = 100
        private const val BOUND_HEIGHT = 100
        private const val BOUND_PADDING = 5

        fun newInstance(trackId: Long): TrackViewFragment =
            TrackViewFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_TRACK_ID, trackId)
                }
            }
    }

    private var googleMap: GoogleMap? = null
    private var savedTracks: List<Track>? = null // TODO: Move to Presenter

//    private var _viewBinding: FragmentTrackViewBinding? = null
//    private val viewBinding: FragmentTrackViewBinding
//    get() = _viewBinding!!

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

        // add marker and other stuff
        savedTracks?.let { tracks ->
            if (tracks.isNotEmpty()) {
                val track = tracks[0]
                showTrack(track)
            }
        }
    }

    override fun setTrack(track: Track) {
        val map = googleMap
        if (null != map) {
            if (track.points.isNotEmpty()) {
                showTrack(track)
            }
        } else {
            savedTracks = listOf(track)
        }
    }

    private fun showMarker(map: GoogleMap, latLng: LatLng, title: String, color: Float): Marker =
        map.addMarker(
            MarkerOptions().position(latLng).title(title).icon(
                BitmapDescriptorFactory.defaultMarker(color)
            )
        )


    private fun showTrack(track: Track) {
        googleMap?.let { map ->
            if (track.points.isNotEmpty()) {
                val polylineOptions = PolylineOptions().width(6.0f).color(Color.BLUE)
                polylineOptions.geodesic(true)
                polylineOptions.addAll(track.points.toLatLngs())
                map.addPolyline(polylineOptions)

                val latLngs = track.points.toLatLngs()
                val markers = mutableListOf<Marker>()
                if (latLngs.lastIndex > 0) {
                   val startMarker =  showMarker(
                        map,
                        latLngs[0],
                        "${track.name} ${resources.getString(R.string.tracks_start)}",
                        BitmapDescriptorFactory.HUE_GREEN
                    )
                    markers.add(startMarker)
                }

                val finishMarker = showMarker(
                    map,
                    latLngs.last(),
                    "${track.name} ${resources.getString(R.string.tracks_finish)}",
                    BitmapDescriptorFactory.HUE_RED
                )
                markers.add(finishMarker)

                val b = LatLngBounds.Builder()
                markers.forEach { marker ->
                    b.include(marker.position)
                }
                val bounds = b.build()
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, BOUND_WIDTH, BOUND_HEIGHT, BOUND_PADDING)
                map.animateCamera(cameraUpdate)
            }
        }
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