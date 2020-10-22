package com.lex.simplequest.device.locationmanager

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.lex.simplequest.domain.exception.LocationConnectionFailedException
import com.lex.simplequest.domain.exception.PermissionDeniedException
import com.lex.simplequest.domain.locationmanager.LocationManager
import com.lex.simplequest.domain.locationmanager.model.Location
import com.lex.simplequest.domain.permission.repository.PermissionChecker
import com.lex.simplequest.domain.repository.LocationRepository
import java.lang.IllegalStateException

class LocationManagerImpl(
    ctx: Context,
    //private val locationRepository: LocationRepository,
    private val permissionChecker: PermissionChecker,
) : LocationManager {

    companion object {
        private const val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
        const val UPDATE_INTERVAL = 5000L
        const val FASTEST_INTERVAL = 5000L
    }

    private val context = ctx
    private val googleApiClient: GoogleApiClient
    private var locationRequest: LocationRequest? = null

    private var locationManagerCallback: LocationManager.Callback? = null

    private val connectionCallbacks = object : GoogleApiClient.ConnectionCallbacks {
        @SuppressLint("MissingPermission")
        override fun onConnected(bundle: Bundle?) {
            Log.i("qaz", "M GoogleApiClient connected")
            if (!permissionChecker.checkAnyPermissionGranted(setOf(PermissionChecker.Permission.ACCESS_COARSE_LOCATION, PermissionChecker.Permission.ACCESS_FINE_LOCATION))) {
                // TODO: call callback fun onPermissionRequired
                throw PermissionDeniedException("Location permissions was not granted")
            }
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
            locationManagerCallback?.onConnected()
        }

        override fun onConnectionSuspended(reason: Int) {
            Log.w("qaz", "onConnectionSuspended: $reason")
            locationManagerCallback?.onConnectionSuspended(reason)
        }
    }

    private val connectionFailedListener =
        GoogleApiClient.OnConnectionFailedListener { result ->
            Log.e("qaz", "onConnectionFailed: ${result.errorMessage}")
            locationManagerCallback?.onConnectionFailed(LocationConnectionFailedException(result.errorMessage))
        }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationAvailability(availability: LocationAvailability) {
            super.onLocationAvailability(availability)
            Log.i("qaz", "onLocationAvailability: ${availability.isLocationAvailable}")
            locationManagerCallback?.onLocationAvailable(availability.isLocationAvailable)
        }

        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            Log.d("qaz", "location result: $result in thread: ${Thread.currentThread().name}")

            val location = result?.lastLocation

            if (null != location) {
                // TODO: Write location in DB
                val l = Location(location.latitude, location.longitude, location.altitude)
                locationManagerCallback?.onLocationChanged(l)
            }
        }
    }

    init {
        Log.i("qaz", "M init")
        googleApiClient = initGoogleApiClient()
        if (!checkPlayServices()) {
            throw IllegalStateException("You need to install Google Play Services to use the App properly")
        }
    }

    override fun isConnected(): Boolean =
        null != locationRequest

    override fun connect(callback: LocationManager.Callback?) {
        Log.i("qaz", "connect called")
        if (!googleApiClient.isConnected) {
            googleApiClient.connect()
            locationManagerCallback = callback
        } else {
            Log.d("qaz", "already connected")
        }
    }

    override fun disconnect() {
        Log.w("qaz", "disconnect called")
        if (googleApiClient.isConnected) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.removeLocationUpdates(locationCallback)
            googleApiClient.disconnect()
            locationRequest = null
            locationManagerCallback = null
        } else {
            Log.d("qaz", "already disconnected")
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

}