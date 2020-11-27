package com.lex.simplequest.domain.model

import android.location.Location
import com.google.android.gms.maps.model.LatLng

data class Point(
    val id: Long,
    val trackId: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val timestamp: Long
)

fun Point.toLatLng() =
    LatLng(this.latitude, this.longitude)

fun List<Point>.toLatLngs() =
    this.map {
        LatLng(it.latitude, it.longitude)
    }

fun Point.distanceTo(otherPoint: Point): Float {
    val results = FloatArray(3)
    var distanceInMeters = .0f
    Location.distanceBetween(
        this.latitude,
        this.longitude,
        otherPoint.latitude,
        otherPoint.longitude,
        results
    )

    return results[0]
}