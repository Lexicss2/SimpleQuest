package com.lex.simplequest.domain.locationmanager

import com.lex.simplequest.domain.locationmanager.model.Location

interface LocationManager {
    fun isConnected(): Boolean
    fun connect(callback: Callback?)
    fun disconnect()

    interface Callback {
        fun onConnected()
        fun onConnectionSuspended(reason: Int)
        fun onConnectionFailed(error: Throwable)
        fun onLocationChanged(location: Location)
        fun onLocationAvailable(available: Boolean)
    }
}