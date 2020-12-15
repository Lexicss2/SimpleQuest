package com.lex.simplequest.device.locationmanager

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Looper
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.lex.simplequest.Config
import com.lex.simplequest.domain.exception.LocationConnectionFailedException
import com.lex.simplequest.domain.exception.PermissionDeniedException
import com.lex.simplequest.domain.locationmanager.LocationManager
import com.lex.simplequest.domain.locationmanager.model.Location
import com.lex.simplequest.domain.permission.repository.PermissionChecker
import java.lang.IllegalStateException
import kotlin.math.min

class LocationManagerImpl(
    ctx: Context,
    private val permissionChecker: PermissionChecker,
) : LocationManager {

    companion object {
        private const val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
        private const val FASTEST_INTERVAL = 5000L
    }

    private val context = ctx
    private val googleApiClient: GoogleApiClient
    private var locationRequest: LocationRequest? = null

    private var locationManagerCallback: LocationManager.Callback? = null
    private var connectionConfig: LocationManager.ConnectionConfig? = null

    private val connectionCallbacks = object : GoogleApiClient.ConnectionCallbacks {
        @SuppressLint("MissingPermission")
        override fun onConnected(bundle: Bundle?) {
            if (!permissionChecker.checkAnyPermissionGranted(setOf(PermissionChecker.Permission.ACCESS_COARSE_LOCATION, PermissionChecker.Permission.ACCESS_FINE_LOCATION))) {
                // TODO: call callback fun onPermissionRequired
                throw PermissionDeniedException("Location permissions was not granted")
            }
            val config = connectionConfig
            val timeInterval = config?.timePeriodMs ?: Config.DEFAULT_GPS_ACCURACY_TIME_PERIOD_MS
            val displacement = config?.displacement ?: Config.DEFAULT_MINIMAL_DISPLACEMENT
            locationRequest = LocationRequest().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = timeInterval
                fastestInterval = min(timeInterval, FASTEST_INTERVAL)
                smallestDisplacement = displacement.toFloat()
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
            locationManagerCallback?.onConnectionSuspended(reason)
        }
    }

    private val connectionFailedListener =
        GoogleApiClient.OnConnectionFailedListener { result ->
            locationManagerCallback?.onConnectionFailed(LocationConnectionFailedException(result.errorMessage))
        }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationAvailability(availability: LocationAvailability) {
            super.onLocationAvailability(availability)
            locationManagerCallback?.onLocationAvailable(availability.isLocationAvailable)
        }

        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            val location = result?.lastLocation

            if (null != location) {
                val l = Location(location.latitude, location.longitude, location.altitude)
                locationManagerCallback?.onLocationChanged(l)
            }
        }
    }

    init {
        googleApiClient = initGoogleApiClient()
        if (!checkPlayServices()) {
            throw IllegalStateException("You need to install Google Play Services to use the App properly")
        }
    }

    override fun isConnected(): Boolean =
        null != locationRequest

    override fun connect(connectionConfig: LocationManager.ConnectionConfig?, callback: LocationManager.Callback?) {
        if (!googleApiClient.isConnected) {
            this.connectionConfig = connectionConfig
            googleApiClient.connect()
            locationManagerCallback = callback
        }
    }

    override fun disconnect() {
        if (googleApiClient.isConnected) {
            locationManagerCallback?.onLocationAvailable(false)
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.removeLocationUpdates(locationCallback)
            googleApiClient.disconnect()
            locationRequest = null
            locationManagerCallback = null
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