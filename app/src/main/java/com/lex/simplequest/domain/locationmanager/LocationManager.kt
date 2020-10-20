package com.lex.simplequest.domain.locationmanager

import com.lex.simplequest.domain.locationmanager.model.Location

interface LocationManager {
    fun prepare(callback: Callback)
    //fun isPlayServicesInstalled(): Boolean
    fun isConnected(): Boolean
    fun connect()
    fun disconnect()

    interface Callback {
        fun onConnected()
        fun onConnectionSuspended(reason: Int)
        fun onConnectionFailed(error: Throwable)
        fun onLocationChanged(location: Location)
    }
}