package com.lex.simplequest.data.settings.repository

import android.content.Context
import android.content.SharedPreferences
import com.lex.simplequest.Config
import com.lex.simplequest.domain.repository.SettingsRepository

class SettingsRepositoryImpl(ctx: Context) : SettingsRepository {

    companion object {
        private const val PREF_SETTINGS_FILE = "common_settings"
        private const val PREF_TIME_PERIOD = "time_period"
        private const val PREF_RECORD_DISTANCE_SENSITIVITY = "record_distance_sensitivity"
        private const val PREF_MINIMAL_DISPLACEMENT = "minimal_displacement"
        private const val PREF_MINIMAL_BATTERY_LEVEL = "minimal_battery_level"
    }

    private val context = ctx
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_SETTINGS_FILE, Context.MODE_PRIVATE)

    override fun getTimePeriod(): Long =
        prefs.getLong(PREF_TIME_PERIOD, Config.DEFAULT_GPS_ACCURACY_TIME_PERIOD_MS)


    override fun setTimePeriod(value: Long) {
        prefs.edit()
            .putLong(PREF_TIME_PERIOD, value)
            .apply()
    }

    override fun getRecordDistanceSensitivity(): Long =
        prefs.getLong(PREF_RECORD_DISTANCE_SENSITIVITY, Config.DEFAULT_MINIMAL_TRACK_DISTANCE_M)

    override fun setRecordDistanceSensitivity(value: Long) {
        prefs.edit()
            .putLong(PREF_RECORD_DISTANCE_SENSITIVITY, value)
            .apply()
    }

    override fun getMinimalDisplacement(): Long =
        prefs.getLong(PREF_MINIMAL_DISPLACEMENT, Config.DEFAULT_MINIMAL_DISPLACEMENT)

    override fun setMinimalDisplacement(value: Long) {
        prefs.edit()
            .putLong(PREF_MINIMAL_DISPLACEMENT, value)
            .apply()
    }

    override fun getMinimalBatteryLevel(): Int =
        prefs.getInt(PREF_MINIMAL_BATTERY_LEVEL, Config.DEFAULT_BATTERY_LEVEL_PC)

    override fun setMinimalBatteryLevel(value: Int) {
        prefs.edit()
            .putInt(PREF_MINIMAL_BATTERY_LEVEL, value)
            .apply()
    }
}