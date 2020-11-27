package com.lex.simplequest.presentation.utils

import android.graphics.Color
import com.google.android.gms.maps.model.*
import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.domain.model.toLatLng
import com.lex.simplequest.domain.model.toLatLngs

private const val DASH_LENGTH = 10.0f
private const val GAP_LENGTH = 10.0f

fun Track.toPolylineOptions(isRecording: Boolean, width: Float): List<PolylineOptions> {
    val polylineOptionsList = mutableListOf<PolylineOptions>()
    val pathes = this.pathes
    var lastPoint: LatLng? = null
    val color = if (isRecording) Color.RED else Color.BLUE
    val dashPattern: PatternItem = Dash(DASH_LENGTH)
    val gapPattern: PatternItem = Gap(GAP_LENGTH)
    val patterns = listOf(dashPattern, gapPattern)

    pathes.forEach { path ->
        if (null != lastPoint) {
            // draw dotted
            if (path.isNotEmpty()) {
                val firstPoint = path.first().toLatLng()
                val dashedPolylineOptions = PolylineOptions()
                    .width(width)
                    .color(color)
                    .pattern(patterns)
                    .geodesic(true)
                    .add(lastPoint)
                    .add(firstPoint)
                polylineOptionsList.add(dashedPolylineOptions)
            }
        }

        val points = path.toLatLngs()
        if (points.isNotEmpty()) {
            val solidLineOptions = PolylineOptions()
                .width(width)
                .color(color)
                .geodesic(true)
                .addAll(points)

            polylineOptionsList.add(solidLineOptions)
            lastPoint = points.last()
        }
    }

    return polylineOptionsList.toList()
}

fun List<Polyline>.toLatLngBounds(): LatLngBounds {
    val boundsBuilder = LatLngBounds.Builder()
    this.forEach { polyline ->
        polyline.points.forEach {
            boundsBuilder.include(it)
        }
    }

    return boundsBuilder.build()
}

fun List<Polyline>.canBeDrawn(): Boolean {
    this.forEach { polyline ->
        if (polyline.points.size > 1) {
            return true
        }
    }

    return false
}
