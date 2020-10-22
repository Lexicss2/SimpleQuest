package com.lex.simplequest.domain.locationmanager

import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.domain.repository.LocationRepository

interface LocationTracker {
    fun testMethod() // TODO: Remove it
    fun setup(lm: LocationManager, lr: LocationRepository)
    fun startRecording()
    fun stopRecording()
    fun isRecording(): Boolean
    fun getLastTrack(): Track?

    var locationTrackerListener: Listener?

    interface Listener {
        fun onLocationManagerConnected()
        fun onLocationMangerConnectionSuspended(reason: Int)
        fun onLocationMangerConnectionFailed(error: Throwable)
        fun onTrackUpdated(track: Track)
    }
}