package com.lex.simplequest.data.location.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import androidx.core.database.getDoubleOrNull
import com.lex.simplequest.device.content.provider.QuestContract
import com.lex.simplequest.domain.model.Point
import com.lex.simplequest.domain.model.Track

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
            null,
            "${QuestContract.Points.COLUMN_TRACK_ID} = $trackId",
            null,
            null
        )
        val points = pointsCursor?.use {
            it.toPoints()
        } ?: emptyList()

        val id = this.getLong(this.getColumnIndex(QuestContract.Tracks.COLUMN_ID))
        val name = this.getString(this.getColumnIndex(QuestContract.Tracks.COLUMN_NAME))
        val startTime =
            this.getLong(this.getColumnIndex(QuestContract.Tracks.COLUMN_START_TIME))
        val indexStopTime = this.getColumnIndex(QuestContract.Tracks.COLUMN_END_TIME)
        val entTime = if (!this.isNull(indexStopTime)) this.getLong(indexStopTime) else null
        Track(id, name, startTime, entTime, points)
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

private fun Cursor.getTrackId() = getLong(getColumnIndex(QuestContract.Tracks.COLUMN_ID))

private fun Cursor.getTrackPointId() = getLong(getColumnIndex(QuestContract.Points.COLUMN_ID))

private fun <T> Cursor.toList(toT: Cursor.() -> T): List<T> =
    if (moveToFirst()) {
        (1..count).map {
            toT().apply {
                moveToNext()
            }
        }
    } else arrayListOf()
