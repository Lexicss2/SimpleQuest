package com.lex.simplequest.domain.model

import android.content.Context
import android.location.Location
import android.os.Environment
import com.lex.simplequest.Config
import com.lex.simplequest.presentation.utils.toDateString
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter

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

