package com.lex.simplequest.device.locationmanager

import android.content.Context
import android.location.Location
import android.os.Bundle
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiActivity
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.lex.simplequest.domain.locationmanager.LocationManager
import java.lang.IllegalStateException

class LocationManagerImpl(ctx: Context) : LocationManager {
    private val context = ctx

    private var callback: LocationManager.Callback? = null
    private var googleApiClient: GoogleApiClient? = null
    private var lastLocation: Location? = null

    private val connectionCallbacks = object : GoogleApiClient.ConnectionCallbacks {

        override fun onConnected(bundle: Bundle?) {
            // TODO: Update deprecated logic
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
            callback?.onConnected()
        }

        override fun onConnectionSuspended(reason: Int) {
            TODO("Not yet implemented")
        }

    }

    private val connectionFailedListener = object : GoogleApiClient.OnConnectionFailedListener {
        override fun onConnectionFailed(connectionResult: ConnectionResult) {
            TODO("Not yet implemented")
        }
    }



//    private val onSuccessListener = object : OnSuccessListener<Any>){
//    }

    override fun isConnected(): Boolean =
        false

    override fun prepare(callback: LocationManager.Callback) {
        this.callback = callback

        val googleApiClientBuilder = GoogleApiClient.Builder(context).apply {
            addApi(LocationServices.API)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(connectionFailedListener)
        }
        googleApiClient = googleApiClientBuilder.build()

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        //fusedLocationClient.
    }

//    override fun isPlayServicesInstalled(): Boolean {
//        val apiAvailability = GoogleApiAvailability.getInstance()
//        val resultCode = apiAvailability.isGooglePlayServicesAvailable(context)
//        return ConnectionResult.SUCCESS == resultCode
//    }

    override fun connect() {
        googleApiClient?.let { client ->
            client.connect()
        }
            ?: throw IllegalStateException("LocationManager is not prepared. Prepare it first before connect")
    }

    override fun disconnect() {
        TODO("Not yet implemented")
    }
}