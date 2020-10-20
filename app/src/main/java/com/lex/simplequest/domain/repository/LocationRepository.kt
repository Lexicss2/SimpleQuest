package com.lex.simplequest.domain.repository

import com.lex.simplequest.domain.model.Track
import java.io.Closeable

interface LocationRepository : Closeable {
    fun isLocationServicesEnabled(): Boolean

    fun startTrack(name: String): Long
    fun stopTrack(id: Long)
    fun getTrack(id: Long): Track?
    fun updateTrack(track: Track): Boolean
    fun deleteTrack(id: Long): Boolean

    interface OnUpdateListener {
        fun onUpdated()
    }
}