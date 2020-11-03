package com.lex.simplequest.data.location.repository.queries

import com.lex.simplequest.data.location.repository.LocationQuerySpecificationImpl
import com.lex.simplequest.device.content.provider.QuestContract

class TrackByIdQuerySpecification(private val trackId: Long) : LocationQuerySpecificationImpl {

    override fun getWhereClause(): String =
        QuestContract.Tracks.COLUMN_ID + " = " + trackId.toString()
// SELECT * FROM tracks WHERE start_time = (SELECT MAX(start_time) FROM tracks)
//    SELECT * FROM tracks WHERE end_time is NULL
}