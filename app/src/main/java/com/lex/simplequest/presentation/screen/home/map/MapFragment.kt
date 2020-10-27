package com.lex.simplequest.presentation.screen.home.map

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
import com.lex.simplequest.presentation.base.BaseMvpFragment
import com.lex.simplequest.presentation.screen.home.MainRouter
import com.softeq.android.mvp.PresenterStateHolder
import com.softeq.android.mvp.VoidPresenterStateHolder


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

    //private lateinit var googleMapFragment: SupportMapFragment
    private var map: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        inflater.inflate(R.layout.fragment_map, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initGoogleMap()
        super.onViewCreated(view, savedInstanceState)
    }

    // Draw path on Map
    //https://stackoverflow.com/questions/2176397/drawing-a-line-path-on-google-maps


    override fun onMapReady(googleMap: GoogleMap) {
        Log.i("qaz", "MAP IS READY")
        map = googleMap


        // Temporary
        val minsk = LatLng(53.905, 27.606)
        val opt = MarkerOptions()

        val markerMinsk = map!!.addMarker(MarkerOptions().position(minsk).title("Marker in Sydney").icon(
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
        ))

        val dacha = LatLng(54.022637, 27.610355)
        val markerDacha = map!!.addMarker(MarkerOptions().position(dacha).title("dacha").icon(
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
        ))

        map!!.moveCamera(CameraUpdateFactory.newLatLng(minsk))
    }

    override fun setPin(data: Any) {

    }

    private fun initGoogleMap() {
        if (null == map) {
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
        VoidPresenterStateHolder()

}