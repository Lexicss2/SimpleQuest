package com.lex.simplequest.domain.common.connectivity

import java.io.Closeable

interface InternetConnectivityTracker : Closeable {
    val isInternetConnected: Boolean
    fun addInternetConnectivityChangedListener(listener: OnInternetConnectivityChangedListener)
    fun removeInternetConnectivityChangedListener(listener: OnInternetConnectivityChangedListener)

    interface OnInternetConnectivityChangedListener {
        fun onInternetConnectivityChanged(internetConnectivityTracker: InternetConnectivityTracker)
    }
}