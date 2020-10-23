package com.lex.simplequest.data.location.repository

import android.content.ContentValues
import android.database.Cursor
import com.lex.simplequest.device.content.provider.QuestContract
import com.lex.simplequest.domain.model.Point
import com.lex.simplequest.domain.model.Track

fun Track.toContentValues(): ContentValues =
    ContentValues().apply {
        put(QuestContract.Tracks.COLUMN_NAME, name)
        put(QuestContract.Tracks.COLUMN_START_TIME, startTime)
        put(QuestContract.Tracks.COLUMN_END_TIME, endTime)
    }


fun Cursor.toTracks(): List<Track> =
    if (moveToFirst()) {
        val tracksList = mutableListOf<Track>()
        do {
            val idIndex = getColumnIndex(QuestContract.Tracks.COLUMN_ID)
            val nameIndex = getColumnIndex(QuestContract.Tracks.COLUMN_NAME)
            val id = if (idIndex > -1) {
                getLong(idIndex)
            } else 0L
            //val id = getLong(idIndex)

            val name = getString(nameIndex)
            val startTime = getLong(getColumnIndex(QuestContract.Tracks.COLUMN_START_TIME))
            val indexStopTime = getColumnIndex(QuestContract.Tracks.COLUMN_END_TIME)
            val entTime = if (!isNull(indexStopTime)) getLong(indexStopTime) else null
            val track = Track(id, name, startTime, entTime)
            tracksList.add(track)
        } while (moveToNext())
        tracksList.toList()
    } else {
        emptyList()
    }

fun Cursor.toTrack(): Track? {
    val tracks = toTracks()
    return if (tracks.isNotEmpty()) {
        tracks[0]
    } else null
}

fun Point.toContentValues(): ContentValues =
    ContentValues().apply {
        put(QuestContract.Points.COLUMN_LATITUDE, latitude)
        put(QuestContract.Points.COLUMN_LONGITUDE, longitude)
        put(QuestContract.Points.COLUMN_ALTITUDE, altitude)
        put(QuestContract.Points.COLUMN_TRACK_ID, trackId)
    }
