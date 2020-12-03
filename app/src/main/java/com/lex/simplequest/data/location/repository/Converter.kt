package com.lex.simplequest.data.location.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import androidx.core.database.getDoubleOrNull
import com.lex.simplequest.device.content.provider.QuestContract
import com.lex.simplequest.domain.model.CheckPoint
import com.lex.simplequest.domain.model.Point
import com.lex.simplequest.domain.model.Track
import java.lang.IllegalStateException

fun Track.toContentValues(): ContentValues =
    ContentValues().apply {
        put(QuestContract.Tracks.COLUMN_NAME, name)
        put(QuestContract.Tracks.COLUMN_START_TIME, startTime)
        put(QuestContract.Tracks.COLUMN_END_TIME, endTime)
    }

fun Cursor.toFullTracks(context: Context): List<Track> =
    this.toList {
        val trackId = this.getTrackId()
        val pointsCursor = context.contentResolver.query(
            QuestContract.Points.CONTENT_URI,
            QuestContract.Points.PROJECTION,
            "${QuestContract.Points.COLUMN_TRACK_ID} = $trackId",
            null,
            null
        )
        val checkPointsCursor = context.contentResolver.query(
            QuestContract.CheckPoints.CONTENT_URI,
            QuestContract.CheckPoints.PROJECTION,
            "${QuestContract.Points.COLUMN_TRACK_ID} = $trackId",
            null,
            null
        )
        val points = pointsCursor?.use {
            it.toPoints()
        } ?: emptyList()

        val checkPoints = checkPointsCursor?.use {
            it.toCheckPoints()
        } ?: emptyList()

        val id = this.getLong(this.getColumnIndex(QuestContract.Tracks.COLUMN_ID))
        val name = this.getString(this.getColumnIndex(QuestContract.Tracks.COLUMN_NAME))
        val startTime =
            this.getLong(this.getColumnIndex(QuestContract.Tracks.COLUMN_START_TIME))
        val indexStopTime = this.getColumnIndex(QuestContract.Tracks.COLUMN_END_TIME)
        val entTime = if (!this.isNull(indexStopTime)) this.getLong(indexStopTime) else null
        Track(id, name, startTime, entTime, points, checkPoints)
    }

fun Point.toContentValues(): ContentValues =
    ContentValues().apply {
        put(QuestContract.Points.COLUMN_LATITUDE, latitude)
        put(QuestContract.Points.COLUMN_LONGITUDE, longitude)
        put(QuestContract.Points.COLUMN_ALTITUDE, altitude)
        put(QuestContract.Points.COLUMN_TRACK_ID, trackId)
        put(QuestContract.Points.COLUMN_TIMESTAMP, timestamp)
    }

fun Cursor.toPoints(): List<Point> =
    this.toList {
        val pointId = this.getTrackPointId()
        val trackId = this.getLong(getColumnIndex(QuestContract.Points.COLUMN_TRACK_ID))
        val latitude = this.getDouble(getColumnIndex(QuestContract.Points.COLUMN_LATITUDE))
        val longitude =
            this.getDouble(getColumnIndex(QuestContract.Points.COLUMN_LONGITUDE))
        val altitude =
            this.getDoubleOrNull(getColumnIndex(QuestContract.Points.COLUMN_ALTITUDE))
        val timeStamp = getLong(getColumnIndex(QuestContract.Points.COLUMN_TIMESTAMP))

        Point(pointId, trackId, latitude, longitude, altitude, timeStamp)
    }

fun CheckPoint.toContentValue(): ContentValues =
    ContentValues().apply {
        put(QuestContract.CheckPoints.COLUMN_TRACK_ID, trackId)
        put(QuestContract.CheckPoints.COLUMN_TYPE, type.asInt())
        put(QuestContract.CheckPoints.COLUMN_TIMESTAMP, timestamp)
        put(QuestContract.CheckPoints.COLUMN_TAG, tag)
    }

fun Cursor.toCheckPoints(): List<CheckPoint> =
    this.toList {
        val checkPointId = this.getCheckPointId()
        val trackId = this.getLong(getColumnIndex(QuestContract.CheckPoints.COLUMN_TRACK_ID))
        val typeInt = this.getInt(getColumnIndex(QuestContract.CheckPoints.COLUMN_TYPE))
        val type = typeInt.asCheckPointType()
        val timestamp = getLong(getColumnIndex(QuestContract.CheckPoints.COLUMN_TIMESTAMP))
        val indexTag = this.getColumnIndex(QuestContract.CheckPoints.COLUMN_TAG)
        val tag = if (!this.isNull(indexTag)) this.getString(indexTag) else null
        CheckPoint(checkPointId, trackId, type, timestamp, tag)
    }


private fun Cursor.getTrackId() = getLong(getColumnIndex(QuestContract.Tracks.COLUMN_ID))

private fun Cursor.getTrackPointId() = getLong(getColumnIndex(QuestContract.Points.COLUMN_ID))

private fun Cursor.getCheckPointId() = getLong(getColumnIndex(QuestContract.CheckPoints.COLUMN_ID))

private fun <T> Cursor.toList(toT: Cursor.() -> T): List<T> =
    if (moveToFirst()) {
        (1..count).map {
            toT().apply {
                moveToNext()
            }
        }
    } else arrayListOf()

private fun CheckPoint.Type.asInt() =
    when (this) {
        CheckPoint.Type.PAUSE -> 0
        CheckPoint.Type.RESUME -> 1
    }

private fun Int.asCheckPointType(): CheckPoint.Type =
    when (this) {
        0 -> CheckPoint.Type.PAUSE
        1 -> CheckPoint.Type.RESUME
        else -> throw IllegalStateException("No CheckPoint type for int $this")
    }
