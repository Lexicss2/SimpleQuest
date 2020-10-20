package com.lex.simplequest.device.connectivity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.lex.simplequest.domain.common.connectivity.InternetConnectivityTracker
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicBoolean

class InternetConnectivityTrackerImpl(ctx: Context) : InternetConnectivityTracker {

    private val context = ctx.applicationContext
    private val isClosed = AtomicBoolean(false)
    private val listeners = CopyOnWriteArraySet<InternetConnectivityTracker.OnInternetConnectivityChangedListener>()

    override val isInternetConnected: Boolean
        get() {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }

    private val networkChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            listeners.forEach { it.onInternetConnectivityChanged(this@InternetConnectivityTrackerImpl) }
        }
    }

    override fun addInternetConnectivityChangedListener(listener: InternetConnectivityTracker.OnInternetConnectivityChangedListener) {
        listeners.add(listener)
    }

    override fun removeInternetConnectivityChangedListener(listener: InternetConnectivityTracker.OnInternetConnectivityChangedListener) {
        listeners.remove(listener)
    }

    override fun close() {
        if (!isClosed.getAndSet(true)) {
            context.unregisterReceiver(networkChangeReceiver)
        }
    }
}