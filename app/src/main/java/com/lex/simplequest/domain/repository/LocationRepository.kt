package com.lex.simplequest.domain.repository

import com.lex.simplequest.domain.model.Track
import java.io.Closeable

interface LocationRepository : Closeable {
    fun isLocationServicesEnabled(): Boolean

    // TODO: Refactor. Remove getTrack
    fun startTrack(name: String, startTime: Long): Long
    fun stopTrack(id: Long, endTime: Long): Boolean
    fun getTrack(id: Long): Track?
    fun getTracks(spec: LocationQuerySpecification): List<Track>
    fun updateTrack(track: Track): Boolean
    fun deleteTrack(id: Long): Boolean
    fun addPoint(trackId: Long, latitude: Double, longitude: Double, altitude: Double?)

    fun getQuerySpecificationFactory(): LocationQuerySpecificationFactory

    interface LocationQuerySpecification

    interface LocationQuerySpecificationFactory {
        fun trackById(trackId: Long): LocationQuerySpecification
        fun trackByName(trackName: String): LocationQuerySpecification
        fun allTracks(): LocationQuerySpecification
    }

    interface OnUpdateListener {
        fun onUpdated()
    }
}