package com.lex.simplequest.data.location.repository

import android.content.ContentValues
import com.lex.simplequest.device.content.provider.QuestContract
import com.lex.simplequest.domain.model.Track

fun Track.toContentValues(): ContentValues =
    ContentValues().apply {
        put(QuestContract.Tracks.COLUMN_NAME, name)
        put(QuestContract.Tracks.COLUMN_START_TIME, startTime)
    }