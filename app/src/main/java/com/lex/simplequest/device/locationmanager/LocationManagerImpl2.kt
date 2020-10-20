package com.lex.simplequest.device.locationmanager

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.lex.simplequest.domain.locationmanager.LocationManager
import com.lex.simplequest.domain.repository.LocationRepository
import java.lang.IllegalStateException

class LocationManagerImpl2(
    ctx: Context,
    private val locationRepository: LocationRepository
) : LocationManager {

    companion object {
        private const val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
        const val UPDATE_INTERVAL = 5000L
        const val FASTEST_INTERVAL = 5000L
    }

    private val context = ctx
    private val googleApiClient: GoogleApiClient
    private var locationRequest: LocationRequest? = null
    //private var isGooglePlayServicesInstalled: Boolean = false

    init {
        googleApiClient = initGoogleApiClient()
        if (!checkPlayServices()) {
            throw IllegalStateException("You need to install Google Play Services to use the App properly")
        }
    }

    private fun initGoogleApiClient(): GoogleApiClient =
        GoogleApiClient.Builder(context).apply {
            addApi(LocationServices.API)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(connectionFailedListener)
        }.build()

    private fun checkPlayServices(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(context)

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(
                    context as Activity,
                    resultCode,
                    PLAY_SERVICES_RESOLUTION_REQUEST
                )
            } else {
                throw IllegalStateException("PlayServices are not installed (error not resolvable)")
            }

            return false
        }

        return true
    }


    private val connectionCallbacks = object : GoogleApiClient.ConnectionCallbacks {
        override fun onConnected(bundle: Bundle?) {
            locationRequest = LocationRequest().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = UPDATE_INTERVAL
                fastestInterval = FASTEST_INTERVAL
            }

            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }

        override fun onConnectionSuspended(reason: Int) {
            Log.w("qaz", "onConnectionSuspended: $reason")
        }
    }

    private val connectionFailedListener =
        GoogleApiClient.OnConnectionFailedListener { result ->
            Log.e("qaz", "onConnectionFailed: ${result.errorMessage}")
        }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationAvailability(availability: LocationAvailability) {
            super.onLocationAvailability(availability)
            Log.i("qaz", "onLocationAvailability: ${availability.isLocationAvailable}")
        }

        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            Log.d("qaz", "location result: $result")

            val location = result?.let { r ->
                r.lastLocation
            }

            if (null != location) {
                // TODO: Write location in DB
            }
        }
    }

    override fun prepare(callback: LocationManager.Callback) {

    }

    override fun isConnected(): Boolean =
        null != locationRequest

    override fun connect() {
        googleApiClient.connect()
    }

    override fun disconnect() {
        if (googleApiClient.isConnected) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.removeLocationUpdates(locationCallback)
            locationRequest = null
        }
    }
}