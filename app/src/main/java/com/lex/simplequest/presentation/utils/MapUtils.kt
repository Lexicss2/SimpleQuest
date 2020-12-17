package com.lex.simplequest.presentation.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.DisplayMetrics
import android.util.Size
import android.util.TypedValue
import android.view.WindowManager
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

fun screenSize(activity: Activity): Size {
    val tv = TypedValue()
    val actionBarHeight =
        if (activity.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            TypedValue.complexToDimensionPixelSize(tv.data, activity.resources.displayMetrics)
        } else 0

    val resourceId: Int = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
    val statusBarHeight = if (resourceId > 0) activity.resources.getDimensionPixelSize(resourceId) else 0

    val displayMetrics = DisplayMetrics()
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        activity.display?.getRealMetrics(displayMetrics)
    } else {
        val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(displayMetrics)
    }

    return Size(displayMetrics.widthPixels, displayMetrics.heightPixels - actionBarHeight - statusBarHeight)
}
