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

fun List<CheckPoint>.lastPause(): CheckPoint? = if (this.isNotEmpty()) {
    var foundCheckPoint: CheckPoint? = null
    for (i in this.size - 1 downTo 0) {
        val cp = this[i]
        if (CheckPoint.Type.PAUSE == cp.type) {
            foundCheckPoint = cp
            break
        }
    }
    foundCheckPoint
} else null