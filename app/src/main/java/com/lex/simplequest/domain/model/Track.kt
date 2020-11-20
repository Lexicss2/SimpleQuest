package com.lex.simplequest.domain.model

import android.location.Location
import com.lex.simplequest.Config

data class Track(
    val id: Long,
    val name: String,
    val startTime: Long,
    val endTime: Long? = null,
    val points: List<Point> = emptyList()
)

fun Track.duration(): Long =
    if (null != endTime) endTime - startTime else if (points.isNotEmpty()) points[points.lastIndex].timestamp - startTime else 0L

fun Track.distance(): Float =
    if (points.size > 1) {
        val results = FloatArray(3)
        var distanceInMeters = .0f
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
            distanceInMeters += results[0]
        }

        distanceInMeters
    } else .0f

fun Track.averageSpeed(): Float =
    when (points.size) {
        0 -> .0f
        1 -> .0f
        else -> {
            val results = FloatArray(3)
            var averageSpeed = .0f
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
                val d = results[0]
                val t = (end.timestamp - start.timestamp).toFloat()
                val v = (d / Config.METERS_IN_KILOMETER) / (t / (1000.0f * 60.0f * 60.0f))
                averageSpeed += v
            }

            averageSpeed / (points.size - 1)
        }
    }

