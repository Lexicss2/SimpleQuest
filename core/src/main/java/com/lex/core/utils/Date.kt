package com.lex.core.utils

import java.util.*

fun Date.startOfDay(timeZone: TimeZone): Date =
    Calendar.getInstance(timeZone).let { calendar ->
        calendar.time = this@startOfDay
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.time
    }

fun Date.addYears(timeZone: TimeZone,years: Int): Date =
    Calendar.getInstance(timeZone).let { calendar ->
        calendar.time = this@addYears
        calendar.add(Calendar.YEAR, years)
        calendar.time
    }

fun Date.addDays(timeZone: TimeZone, days: Int): Date =
    Calendar.getInstance(timeZone).let { calendar ->
        calendar.time = this@addDays
        calendar.add(Calendar.DATE, days)
        calendar.time
    }

fun Calendar.compareDates(date1: Date, date2: Date): DateComparison {
    val origDate = this.time
    val tomorrowEndDate = this.let { c ->
        c.time = date2
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        c.add(Calendar.DAY_OF_YEAR, 2)
        c.time
    }
    val yesterdayStartDate = this.let { c ->
        c.time = date2
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        c.add(Calendar.DAY_OF_YEAR, -1)
        c.time
    }

    this.time = date1
    val date1Year = this.get(Calendar.YEAR)
    val date1Month = this.get(Calendar.MONTH)
    val date1Day = this.get(Calendar.DAY_OF_MONTH)

    this.time = date2
    val date2Year = this.get(Calendar.YEAR)
    val date2Month = this.get(Calendar.MONTH)
    val date2Day = this.get(Calendar.DAY_OF_MONTH)

    val result = when {
        (date1Year == date2Year && date1Month == date2Month && date1Day == date2Day) -> {
            DateComparison.SAME_DAY
        }
        date1.time >= tomorrowEndDate.time -> DateComparison.FUTURE
        date1.time < yesterdayStartDate.time -> DateComparison.PAST
        date1.time > date2.time -> DateComparison.NEXT_DAY
        else -> DateComparison.PREVIOUS_DAY
    }
    this.time = origDate
    return result
}

fun Calendar.startOfDay(date: Date): Date {
    val origDate = this.time
    this.time = date
    this.set(Calendar.HOUR_OF_DAY, 0)
    this.set(Calendar.MINUTE, 0)
    this.set(Calendar.SECOND, 0)
    this.set(Calendar.MILLISECOND, 0)
    val startOfTheDay = time
    this.time = origDate
    return startOfTheDay
}

fun Calendar.isSameDay(date1: Date, date2: Date): Boolean {
    val origDate = this.time

    this.time = date1
    val date1Year = this.get(Calendar.YEAR)
    val date1Month = this.get(Calendar.MONTH)
    val date1Day = this.get(Calendar.DAY_OF_MONTH)

    this.time = date2
    val date2Year = this.get(Calendar.YEAR)
    val date2Month = this.get(Calendar.MONTH)
    val date2Day = this.get(Calendar.DAY_OF_MONTH)

    this.time = origDate

    return (date1Year == date2Year && date1Month == date2Month && date1Day == date2Day)
}

fun Calendar.getFieldForDate(date: Date, field: Int): Int {
    val origDate = this.time
    this.time = date
    val value = this.get(field)
    this.time = origDate
    return value
}

enum class DateComparison {
    SAME_DAY,
    NEXT_DAY,
    PREVIOUS_DAY,
    FUTURE,
    PAST
}



