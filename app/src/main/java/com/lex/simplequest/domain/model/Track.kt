package com.lex.simplequest.domain.model

import android.content.Context
import android.location.Location
import com.lex.simplequest.Config
import com.lex.simplequest.presentation.utils.toDateString
import java.io.File
import java.io.FileWriter

data class Track(
    val id: Long,
    val name: String,
    val startTime: Long,
    val endTime: Long? = null,
    val points: List<Point> = emptyList(),
    val checkPoints: List<CheckPoint> = emptyList()
) {
    // Get points between pauses in track
    val pathes: List<List<Point>> by lazy {
        if (checkPoints.isNotEmpty()) {
            val pathesList = mutableListOf<List<Point>>()
            var pointsIndex = 0
            var startTime = this.startTime
            var endTime: Long

            checkPoints.forEach { checkPoint ->
                when (checkPoint.type) {
                    CheckPoint.Type.PAUSE -> {
                        endTime = checkPoint.timestamp
                        val subPointsList = mutableListOf<Point>()

                        var point = points[pointsIndex]
                        while (point.timestamp < endTime && pointsIndex < points.size) {
                            if (point.timestamp >= startTime) {
                                subPointsList.add(point)
                            }
                            pointsIndex++

                            if (pointsIndex < points.size) {
                                point = points[pointsIndex]
                            }
                        }

                        pathesList.add(subPointsList)
                    }

                    CheckPoint.Type.RESUME -> {
                        startTime = checkPoint.timestamp
                    }
                }
            }

            val subPointsList = mutableListOf<Point>()
            for (i in pointsIndex until points.size) {
                val point = points[i]
                if (point.timestamp >= startTime) {
                    subPointsList.add(point)
                }
            }
            pathesList.add(subPointsList)

            pathesList.toList()
        } else listOf(points)
    }
}

//fun Track.fullDuration(): Long =
//    if (null != endTime) endTime - startTime else checkPoints.lastPause()?.let { checkPoint ->
//        checkPoint.timestamp - startTime
//    } ?: if (points.isNotEmpty()) points[points.lastIndex].timestamp - startTime else 0L
fun Track.fullDuration(isNow: Boolean = false): Long =
    if (null != endTime) endTime - startTime else if (isNow) System.currentTimeMillis() - startTime else checkPoints.lastPause()?.let { checkPoint ->
        checkPoint.timestamp - startTime
    } ?: if (points.isNotEmpty()) points[points.lastIndex].timestamp - startTime else 0L

fun Track.pausedDuration(): Long = if (checkPoints.isNotEmpty()) {
    var duration = 0L
    var startPause = startTime
    checkPoints.forEach { checkPoint ->
        when (checkPoint.type) {
            CheckPoint.Type.PAUSE -> {
                startPause = checkPoint.timestamp
            }
            CheckPoint.Type.RESUME -> {
                duration += (checkPoint.timestamp - startPause)
            }
        }
    }
    duration
} else 0L

fun Track.movingDuration(isNow: Boolean = false): Long =
    fullDuration(isNow) - pausedDuration()

fun Track.fullDistance(): Float =
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

fun Track.movingDistance(): Float = if (checkPoints.isNotEmpty()) {
    var distance = .0f
    pathes.forEach { path ->
        if (path.size > 1) {
            val firstPoint = path.first()
            val lastPoint = path.last()

            distance += firstPoint.distanceTo(lastPoint)
        }
    }
    distance
} else fullDistance()

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

fun Track.toGpxFile(context: Context): File {
    val outputDir = context.externalCacheDir
    val outputFile = File.createTempFile(this.name, ".gpx", outputDir)
    val fileWriter = FileWriter(outputFile)
    val t = this
    fileWriter.use { writer ->
        writer.apply {
            write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            write("<gpx>\n")
            write("<time>" + t.startTime.toDateString() + "</time>\n")
            write("<trk>\n")
            write("<trkseg>\n")

            t.points.forEach { pt ->
                val openTrkpt = "<trkpt lat=\"${String.format("%.7f", pt.latitude)}\" lon=\"${
                    String.format(
                        "%.7f",
                        pt.longitude
                    )
                }\">\n"
                val time = "<time>${pt.timestamp.toDateString()}</time>\n"
                val closeTrkpt = "</trkpt>\n"
                writer.write("$openTrkpt$time$closeTrkpt")
            }

            write("</trkseg>")
            write("</trk>")
            write("</gpx>")
        }
    }

    return outputFile
}

