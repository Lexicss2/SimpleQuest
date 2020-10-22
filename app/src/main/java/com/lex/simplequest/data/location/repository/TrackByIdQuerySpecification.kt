package com.lex.simplequest.data.location.repository

import com.lex.simplequest.device.content.provider.QuestContract

class TrackByIdQuerySpecification(private val trackId: Long) : LocationQuerySpecificationImpl {

    override fun getWhereClause(): String =
        QuestContract.Tracks.COLUMN_ID + " = " + trackId.toString()

}