package com.lex.simplequest.device.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.lex.simplequest.domain.locationmanager.LocationTracker

class TrackLocationService : Service(), LocationTracker {
    private val binder = TrackLocationBinder()
    private var isActive: Boolean = false

    override fun onBind(intent: Intent?): IBinder? =
        binder

    override fun onCreate() {
        super.onCreate()
        isActive = true
    }

    override fun onDestroy() {
        super.onDestroy()
        isActive = false
    }

    override fun testMethod() {
        Log.i("qaz", "testMethod, isActive: $isActive")
    }

    override fun startRecording() {
        // TODO: Implement, run Location Manager
    }

    override fun stopRecording() {
        // TODO: stop locationManager
    }

    inner class TrackLocationBinder : Binder() {
        fun getService(): TrackLocationService = this@TrackLocationService
    }
}