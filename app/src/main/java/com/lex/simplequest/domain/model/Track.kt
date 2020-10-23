package com.lex.simplequest.domain.model

data class Track(
    val id: Long,
    val name: String,
    val startTime: Long,
    val endTime: Long? = null,
    val points: List<Point> = emptyList()
)

fun Track.duration(): Long =
    if (null != endTime) endTime - startTime else 0L