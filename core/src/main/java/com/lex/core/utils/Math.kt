package com.lex.core.utils

import kotlin.math.abs

fun Int.divideAndRoundUp(divisor: Int): Int {
    val sign = (if (this > 0) 1 else -1) * (if (divisor > 0) 1 else -1)
    return (sign * ((abs(this) + abs(divisor) - 1) / abs(divisor)))
}