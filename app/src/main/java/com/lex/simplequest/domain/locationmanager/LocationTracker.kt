package com.lex.simplequest.domain.locationmanager

import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.domain.repository.LocationRepository

interface LocationTracker {
    fun testMethod() // TODO: Remove it
    fun setup(lm: LocationManager, lr: LocationRepository)
    fun connect()
    fun disconnect()
    fun isConnected(): Boolean
    fun startRecording() // connect and start recording
    fun stopRecording()  // stop recording and disconnect
    fun isRecording(): Boolean
    fun getLastTrack(): Track?

    var locationTrackerListener: Listener?

    interface Listener {
        fun onLocationManagerConnected()
        fun onLocationMangerConnectionSuspended(reason: Int)
        fun onLocationMangerConnectionFailed(error: Throwable)
        fun onTrackUpdated(track: Track)
    }

    enum class Status {
        NONE,          // Service is not created
        IDLE,          // Service is just created only, but not working
        CONNECTING,    // Connecting to Location Manager
        CONNECTED,     // Connected to Location Manager. Tracking GPS location, but not recording
        RECORDING      // Connected and Recording the tracks
    }
}