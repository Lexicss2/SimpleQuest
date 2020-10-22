package com.lex.simplequest.data.location.repository

import com.lex.simplequest.device.content.provider.QuestContract

class TrackByNameQuerySpecification(private val trackName: String) : LocationQuerySpecificationImpl {
    override fun getWhereClause(): String =
        QuestContract.Tracks.COLUMN_NAME + " = '" + trackName + "'"
}