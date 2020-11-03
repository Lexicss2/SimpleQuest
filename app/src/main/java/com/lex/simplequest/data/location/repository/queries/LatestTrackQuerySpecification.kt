package com.lex.simplequest.data.location.repository.queries

import com.lex.simplequest.data.location.repository.LocationQuerySpecificationImpl
import com.lex.simplequest.device.content.provider.QuestContract

class LatestTrackQuerySpecification() : LocationQuerySpecificationImpl {
    override fun getWhereClause(): String =
        QuestContract.Tracks.COLUMN_START_TIME + " = (SELECT MAX(" + QuestContract.Tracks.COLUMN_START_TIME + ") FROM " + QuestContract.Tracks.TABLE_NAME + ")"
}