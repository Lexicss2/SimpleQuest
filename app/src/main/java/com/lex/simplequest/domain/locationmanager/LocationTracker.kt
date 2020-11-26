package com.lex.simplequest.domain.locationmanager

import com.lex.simplequest.domain.locationmanager.model.Location
import com.lex.simplequest.domain.settings.interactor.ReadSettingsInteractor
import com.lex.simplequest.domain.track.interactor.AddCheckPointInteractor
import com.lex.simplequest.domain.track.interactor.AddPointInteractor
import com.lex.simplequest.domain.track.interactor.StartTrackInteractor
import com.lex.simplequest.domain.track.interactor.StopTrackInteractor

interface LocationTracker {
    fun connect(): Boolean
    fun disconnect(): Boolean
    fun isConnecting(): Boolean
    fun isConnected(): Boolean
    fun startRecording()// connect and start recording
    fun stopRecording() // stop recording and disconnect
    fun pauseOrResume()
    fun isRecording(): Boolean
    fun isRecordingPaused(): Boolean
    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
    fun getStatus(): Status

    var recordingEventsListener: LocationTracker.RecordingEventsListener?
    abstract val startTrackInteractor: StartTrackInteractor
    abstract val stopTrackInteractor: StopTrackInteractor
    abstract val addPointInteractor: AddPointInteractor
    abstract val readSettingsInteractor: ReadSettingsInteractor
    abstract val addCheckPointInteractor: AddCheckPointInteractor

    interface Listener {
        fun onLocationManagerConnected()
        fun onLocationMangerConnectionSuspended(reason: Int)
        fun onLocationMangerConnectionFailed(error: Throwable)
        fun onLocationUpdated(location: Location)
        fun onStatusUpdated(status: Status)
        fun onLocationAvailable(isAvailable: Boolean)
    }

    interface RecordingEventsListener {
        fun onRecordStartSucceeded(trackId: Long)
        fun onRecordStartFailed(error: Throwable)
        fun onRecordStopSucceeded(success: Boolean)
        fun onRecordStopFailed(error: Throwable)
        fun onPauseResumeSucceeded(succeeded: Boolean)
        fun onPauseResumeFailed(error: Throwable)
    }

    enum class Status {
        NONE,           // Service is not created
        IDLE,           // Service is just created only, but not working
        RETRIEVING_CONFIG, // Started task to read a config
        CONNECTING,     // Config read succeedded or failed, Connecting to Location Manager
        CONNECTED,      // Connected to Location Manager. Tracking GPS location, but not recording
        RECORDING,       // Connected and Recording the tracks
        PAUSED          // Recording is paused
    }

    data class TrackerConfig(val distanceM: Long, val batteryLevelPc: Int)
}