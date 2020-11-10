package com.lex.simplequest.data.settings.repository

import android.content.Context
import android.content.SharedPreferences
import com.lex.simplequest.domain.repository.SettingsRepository

class SettingsRepositoryImpl(ctx: Context) : SettingsRepository {

    companion object {
        private const val PREF_SETTINGS_FILE = "common_settings"
        private const val PREF_TIME_PERIOD = "time_period"
        private const val PREF_RECORD_DISTANCE_SENSITIVITY = "record_distance_sensitivity"
        private const val DEFAULT_TIME_PERIOD_MS = 5000L
        private const val DEFAULT_RECORD_DISTANCE_SENSITIVITY_M = 0L
    }

    private val context = ctx
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_SETTINGS_FILE, Context.MODE_PRIVATE)

    override fun getTimePeriod(): Long =
        prefs.getLong(PREF_TIME_PERIOD, DEFAULT_TIME_PERIOD_MS)


    override fun setTimePeriod(value: Long) {
        prefs.edit()
            .putLong(PREF_TIME_PERIOD, value)
            .apply()
    }

    override fun getRecordDistanceSensitivity(): Long =
        prefs.getLong(PREF_RECORD_DISTANCE_SENSITIVITY, DEFAULT_RECORD_DISTANCE_SENSITIVITY_M)

    override fun setRecordDistanceSensitivity(value: Long) {
        prefs.edit()
            .putLong(PREF_RECORD_DISTANCE_SENSITIVITY, value)
            .apply()
    }
}