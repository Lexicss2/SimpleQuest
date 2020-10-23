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


//fun Cursor.toTracks(): List<Track> =
//    if (moveToFirst()) {
//        val tracksList = mutableListOf<Track>()
//        do {
//            val id = getLong(getColumnIndex(QuestContract.Tracks.COLUMN_ID))
//            val name = getString(getColumnIndex(QuestContract.Tracks.COLUMN_NAME))
//            val startTime = getLong(getColumnIndex(QuestContract.Tracks.COLUMN_START_TIME))
//            val indexStopTime = getColumnIndex(QuestContract.Tracks.COLUMN_END_TIME)
//            val entTime = if (!isNull(indexStopTime)) getLong(indexStopTime) else null
//
////            val points = getTrackPoints(this, id)
//
//            val track = Track(id, name, startTime, entTime)
//            tracksList.add(track)
//        } while (moveToNext())
//        tracksList.toList()
//    } else {
//        emptyList()
//    }

fun Cursor.toTracks(): List<Track> =
    if (moveToFirst()) {
        val tracksList = mutableListOf<Track>()
        do {
            val id = getLong(getColumnIndex(QuestContract.Tracks.COLUMN_ID))
            val name = getString(getColumnIndex(QuestContract.Tracks.COLUMN_NAME))
            val startTime = getLong(getColumnIndex(QuestContract.Tracks.COLUMN_START_TIME))
            val indexStopTime = getColumnIndex(QuestContract.Tracks.COLUMN_END_TIME)
            val entTime = if (!isNull(indexStopTime)) getLong(indexStopTime) else null

//            val points = getTrackPoints(this, id)

            val track = Track(id, name, startTime, entTime)
            tracksList.add(track)
        } while (moveToNext())
        tracksList.toList()
    } else {
        emptyList()
    }

//private fun getTrackPoints(cursor: Cursor, trackId: Long): List<Point> {
//}

fun Cursor.getTrackId() = getLong(getColumnIndex(QuestContract.Tracks.COLUMN_ID))
fun Cursor.getTrackPointId() = getLong(getColumnIndex(QuestContract.Points.COLUMN_ID))

//fun Cursor.toTrack(): Track? {
//    val tracks = toTracks()
//    return if (tracks.isNotEmpty()) {
//        tracks[0]
//    } else null
//}

fun Point.toContentValues(): ContentValues =
    ContentValues().apply {
        put(QuestContract.Points.COLUMN_LATITUDE, latitude)
        put(QuestContract.Points.COLUMN_LONGITUDE, longitude)
        put(QuestContract.Points.COLUMN_ALTITUDE, altitude)
        put(QuestContract.Points.COLUMN_TRACK_ID, trackId)
    }

fun <T> Cursor.toList(toT: Cursor.() -> T): List<T> =
    if (moveToFirst()) {
        (1..count).map {
            toT().apply {
                moveToNext()
            }
        }
    } else arrayListOf()
