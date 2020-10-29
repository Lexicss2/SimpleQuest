package com.lex.simplequest.domain.model

import android.location.Location
import android.util.Log

data class Track(
    val id: Long,
    val name: String,
    val startTime: Long,
    val endTime: Long? = null,
    val points: List<Point> = emptyList()
)

fun Track.duration(): Long =
    if (null != endTime) endTime - startTime else 0L

fun Track.distance(): Float =
    if (points.size > 1) {
        val results = FloatArray(3)
        var distanceInmeters = .0f
        for (i in 1 until points.size) {
            val start = points[i - 1]
            val end = points[i]
            Location.distanceBetween(
                start.latitude,
                start.longitude,
                end.latitude,
                end.longitude,
                results
            )
            distanceInmeters += results[2]
            //Log.d("qaz", "results = ${results[0]}, ${results[1]}, ${results[2]}")
        }

        distanceInmeters
    } else .0f