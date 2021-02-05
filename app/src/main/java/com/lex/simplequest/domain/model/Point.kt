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
        it.toLatLng()
    }

fun Point.toTimedLocation(): Pair<Long, com.lex.simplequest.domain.locationmanager.model.Location> =
    Pair(this.timestamp, com.lex.simplequest.domain.locationmanager.model.Location(this.latitude, this.longitude, this.altitude))

fun List<Point>.toTimedLocations() =
    this.map {
        it.toTimedLocation()
    }

fun List<Point>.distance(): Float =
    if (this.size > 1) {
        val results = FloatArray(3)
        var distanceInMeters = .0f
        for (i in 1 until  this.size) {
            val a = this[i - 1]
            val b = this[i]
            Location.distanceBetween(
                a.latitude,
                a.longitude,
                b.latitude,
                b.longitude,
                results
            )
            distanceInMeters += results[0]
        }

        distanceInMeters
    } else .0f