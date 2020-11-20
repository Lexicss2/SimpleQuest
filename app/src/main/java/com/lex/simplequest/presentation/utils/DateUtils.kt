package com.lex.simplequest.presentation.utils

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

private val DATE_FORMAT_SHORT = SimpleDateFormat("MM/dd/yyyy", Locale.US)
private val BIRTH_DATE_FORMAT = SimpleDateFormat("MMMM d, yyyy", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}
private val TIME_FORMAT_SHORT = SimpleDateFormat("h:mma", Locale.US)
private val DATE_FORMAT_YEAR = SimpleDateFormat("yyyy", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}
private val DATE_FORMAT_EXPIRATION = SimpleDateFormat("MMMM d, yyyy", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}
private val DATE_FORMAT_CARD_EXPIRATION = SimpleDateFormat("MM/yyyy", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

fun Date.dateShort(): String =
    synchronized(DATE_FORMAT_SHORT) {
        DATE_FORMAT_SHORT.format(this)
    }

fun Date.timeShort(): String =
    synchronized(DATE_FORMAT_SHORT) {
        TIME_FORMAT_SHORT.format(this)
    }

fun showtimeDateFormat(): DateFormat =
    SimpleDateFormat("h:mma", Locale.US)

fun showtimeDateFormat(timeZone: TimeZone): DateFormat =
    SimpleDateFormat("h:mma", Locale.US).apply {
        this.timeZone = timeZone
    }

fun fullDateFormat(timeZone: TimeZone): DateFormat =
    SimpleDateFormat("h:mm a, EEEE, MMMM d", Locale.US).apply {
        this.timeZone = timeZone
    }

fun reservationDateFormat(timeZone: TimeZone): DateFormat =
    SimpleDateFormat("h:mm a, EEEE, MMMM d, yyyy", Locale.US).apply {
        this.timeZone = timeZone
    }

fun Date.asBirthdayDate(): String =
    synchronized(BIRTH_DATE_FORMAT) {
        BIRTH_DATE_FORMAT.format(this@asBirthdayDate)
    }

fun Date.yearString(): String =
    synchronized(DATE_FORMAT_YEAR) {
        DATE_FORMAT_YEAR.format(this@yearString)
    }

fun Date.expirationString(): String =
    synchronized(DATE_FORMAT_EXPIRATION) {
        DATE_FORMAT_EXPIRATION.format(this@expirationString)
    }

fun Date.cardExpirationString(): String =
    synchronized(DATE_FORMAT_CARD_EXPIRATION) {
        DATE_FORMAT_CARD_EXPIRATION.format(this@cardExpirationString)
    }

fun Long.toStringDuration(): String {
    var time = this.toInt()
    val hours = time / 3_600_000
    time -= hours * 3_600_000
    val minutes = time / 60_000
    time -= minutes * 60_000
    val seconds = time / 1_000
    return "${hours.toString().padStart(2, '0')}:${
        minutes.toString().padStart(2, '0')
    }:${seconds.toString().padStart(2, '0')}"
}

// return hh:mm and ss
fun Long.toStringDurations(): Pair<String, String> {
    var time = this.toInt()
    val hours = time / 3_600_000
    time -= hours * 3_600_000
    val minutes = time / 60_000
    time -= minutes * 60_000
    val seconds = time / 1_000
    val minutesStr = "${hours.toString().padStart(2, '0')}:${
        minutes.toString().padStart(2, '0')
    }"
    val secondsStr = seconds.toString().padStart(2, '0')
    return Pair(minutesStr, secondsStr)
}

fun Long.toSingleStringDurations(): String {
    var time = this.toInt()
    val hours = time / 3_600_000
    time -= hours * 3_600_000
    val minutes = time / 60_000
    time -= minutes * 60_000
    val seconds = time / 1_000
    val minutesStr = "${hours.toString().padStart(2, '0')}:${
        minutes.toString().padStart(2, '0')
    }"
    val secondsStr = seconds.toString().padStart(2, '0')
    return "$minutesStr:$secondsStr"
}

fun Long.toDateString(): String {
    val date = Date(this)
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
    return "${dateFormat.format(date)}T${timeFormat.format(date)}Z"
}

