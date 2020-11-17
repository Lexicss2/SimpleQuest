package com.lex.simplequest

object Config {

    @JvmField
    val GPS_ACCURACY_TIME_PERIODS_S = arrayOf("1", "2", "5", "10", "30", "60", "120") // in seconds

    const val DEFAULT_GPS_ACCURACY_TIME_PERIOD_MS = 5000L

    @JvmField
    val MINIMAL_TRACK_DISTANCES_M =
        arrayOf("0", "10", "50", "100", "500", "1000", "2000") // in meters

    const val DEFAULT_MINIMAL_TRACK_DISTANCE_M = 10L

    @JvmField
    val MINIMAL_DISPLACEMENTS_M =
        arrayOf("0", "5", "10", "20", "50", "100", "200") // in meters

    const val DEFAULT_MINIMAL_DISPLACEMENT = 5L
}