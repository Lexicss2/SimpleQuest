package com.lex.simplequest.domain.repository

import com.lex.simplequest.domain.model.CheckPoint
import com.lex.simplequest.domain.model.Point
import com.lex.simplequest.domain.model.Track
import java.io.Closeable

interface LocationRepository : Closeable {
    fun isLocationServicesEnabled(): Boolean

    fun startTrack(name: String, startTime: Long): Long
    fun stopTrack(id: Long, endTime: Long, minimalDistance: Long?): Boolean
    fun getTracks(spec: LocationQuerySpecification): List<Track>
    fun updateTrack(track: Track): Boolean
    fun deleteTrack(id: Long): Boolean
    fun addPoint(point: Point)
    fun addCheckPoint(checkPoint: CheckPoint)

    fun getQuerySpecificationFactory(): LocationQuerySpecificationFactory

    interface LocationQuerySpecification

    interface LocationQuerySpecificationFactory {
        fun trackById(trackId: Long): LocationQuerySpecification
        fun trackByName(trackName: String): LocationQuerySpecification
        fun allTracks(): LocationQuerySpecification
        fun latestTrack(): LocationQuerySpecification
    }

    interface OnUpdateListener {
        fun onUpdated()
    }
}