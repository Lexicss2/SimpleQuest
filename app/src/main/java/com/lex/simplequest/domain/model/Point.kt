package com.lex.simplequest.domain.model

data class Point(
    val id: Long,
    val trackId: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double
)