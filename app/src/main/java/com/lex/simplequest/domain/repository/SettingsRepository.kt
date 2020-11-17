package com.lex.simplequest.domain.repository

interface SettingsRepository {
    fun getTimePeriod(): Long
    fun setTimePeriod(value: Long)
    fun getRecordDistanceSensitivity(): Long
    fun setRecordDistanceSensitivity(value: Long)
    fun getMinimalDisplacement(): Long
    fun setMinimalDisplacement(value: Long)
    fun getMinimalBatteryLevel(): Int
    fun setMinimalBatteryLevel(value: Int)
}