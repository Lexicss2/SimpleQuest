package com.lex.simplequest.domain.model

data class CheckPoint(
    val id: Long,
    val trackId: Long,
    val type: Type,
    val timestamp: Long,
    val tag: String?
) {

    enum class Type {
        PAUSE,
        RESUME
    }
}