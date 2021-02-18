package com.lex.simplequest.domain.locationmanager.model

data class Location(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?
)

fun Location.distance2d(locationB: Location): Float {
    val results = FloatArray(3)
    android.location.Location.distanceBetween(
        this.latitude,
        this.longitude,
        locationB.latitude,
        locationB.longitude,
        results
    )

    return results[0]
}