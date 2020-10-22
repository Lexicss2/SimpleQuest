package com.lex.simplequest.device.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.lex.simplequest.domain.locationmanager.LocationManager
import com.lex.simplequest.domain.locationmanager.LocationTracker
import com.lex.simplequest.domain.locationmanager.model.Location
import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.domain.repository.LocationRepository
import java.text.SimpleDateFormat
import java.util.*

// TODO: Create Interactors
// 1. startTrackInteractor
// 2. updateTrackInteractor
// 3. stopTrackInteractor

class TrackLocationService(/*
    private val locationManager: LocationManager,
    private val locationRepository: LocationRepository
*/) : Service(), LocationTracker {

    private lateinit var locationManager: LocationManager
    private lateinit var locationResitory: LocationRepository
    private val binder = TrackLocationBinder()
    private var isActive: Boolean = false
    private var activeTrack: Track? = null

    private var _locationTrackerListener: LocationTracker.Listener? = null

    override var locationTrackerListener: LocationTracker.Listener?
        get() = _locationTrackerListener
        set(value) {
            _locationTrackerListener = value
        }
    private var locationManaferCallback = object : LocationManager.Callback {

        override fun onConnected() {
            _locationTrackerListener?.onLocationManagerConnected()
        }

        override fun onConnectionSuspended(reason: Int) {
            _locationTrackerListener?.onLocationMangerConnectionSuspended(reason)
        }

        override fun onConnectionFailed(error: Throwable) {
            _locationTrackerListener?.onLocationMangerConnectionFailed(error)
        }

        override fun onLocationChanged(location: Location) {
            if (null != activeTrack) {
                // TODO: RM if Location is recording update current Track
            }
        }

        override fun onLocationAvailable(available: Boolean) {

        }
    }

//    constructor(lm: LocationManager, lr: LocationRepository) : this() {
//        locationManager = lm
//        locationResitory = lr
//    }

    override fun setup(lm: LocationManager, lr: LocationRepository) {
        locationManager = lm
        locationResitory = lr
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.i("qaz", "onBind")
        return binder
    }

    override fun onCreate() {
        Log.i("qaz", "LT onCreate")
        super.onCreate()
        isActive = true
    }

    override fun onDestroy() {
        Log.e("qaz", "LT onDestroy")
        super.onDestroy()
        isActive = false
    }

    override fun testMethod() {
        Log.i("qaz", "testMethod, isActive: $isActive")
    }

    override fun startRecording() {
        // TODO: LM connect
        //  LR create and insert new track
        //
        locationManager.connect(locationManaferCallback)
        val name = generateName()
        locationResitory.startTrack(name)
    }

    override fun stopRecording() {
        // TODO: LM disconnect
        // LR close track
        locationManager.disconnect()
    }

    override fun isRecording(): Boolean =
        locationManager.isConnected()

    override fun getLastTrack(): Track? {
        return null
    }

    inner class TrackLocationBinder : Binder() {
        fun getService(): TrackLocationService = this@TrackLocationService
    }

    private fun generateName(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd_hh_mm_ss_SSS", Locale.US)
        return sdf.format(Date())
    }
}