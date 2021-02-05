package com.lex.simplequest

object Config {

    val DEBUG: Boolean = BuildConfig.DEBUG
    val LOG_ENABLED = DEBUG

    @JvmField
    val AVAILABLE_GPS_ACCURACY_TIME_PERIODS_S = arrayOf("1", "2", "5", "10", "30", "60", "120") // in seconds

    const val DEFAULT_GPS_ACCURACY_TIME_PERIOD_MS = 5000L

    @JvmField
    val AVAILABLE_TRACK_DISTANCES_M =
        arrayOf("0", "10", "50", "100", "500", "1000", "2000") // in meters

    const val DEFAULT_MINIMAL_TRACK_DISTANCE_M = 100L

    @JvmField
    val AVAILABLE_DISPLACEMENTS_M =
        arrayOf("0", "5", "10", "20", "50", "100", "200") // in meters

    const val DEFAULT_MINIMAL_DISPLACEMENT = 10L

    val AVAILABLE_BATTERY_LEVELS =
        arrayOf("0", "5", "10", "20", "30", "40", "50")

    const val DEFAULT_BATTERY_LEVEL_PC = 5

    const val METERS_IN_KILOMETER = 1000.0f
}