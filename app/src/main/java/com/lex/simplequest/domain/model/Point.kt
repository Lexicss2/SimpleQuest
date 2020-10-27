package com.lex.simplequest.domain.model

import com.google.android.gms.maps.model.LatLng

data class Point(
    val id: Long,
    val trackId: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?
)

fun List<Point>.toLatLngs() =
    this.map {
        LatLng(it.latitude, it.longitude)
    }