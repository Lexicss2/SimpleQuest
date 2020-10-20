package com.lex.simplequest.presentation.utils

object Colors {

    /**
     * Transforms color1 to color2 according to multiplier and divider factor
     */
    fun transformColor(color1: Int, color2: Int, multiplier: Int, divider: Int): Int {
        require(multiplier <= divider)
        val a1 = color1.a()
        val r1 = color1.r()
        val g1 = color1.g()
        val b1 = color1.b()
        val a2 = color2.a()
        val r2 = color2.r()
        val g2 = color2.g()
        val b2 = color2.b()
        val a = (a1 + ((a2 - a1) * multiplier) / divider) and 0xff
        val r = (r1 + ((r2 - r1) * multiplier) / divider) and 0xff
        val g = (g1 + ((g2 - g1) * multiplier) / divider) and 0xff
        val b = (b1 + ((b2 - b1) * multiplier) / divider) and 0xff
        return (a shl 24) or (r shl 16) or (g shl 8) or (b)
    }

    private fun Int.a(): Int = (this ushr 24) and 0xff
    private fun Int.r(): Int = (this ushr 16) and 0xff
    private fun Int.g(): Int = (this ushr 8) and 0xff
    private fun Int.b(): Int = (this) and 0xff
}