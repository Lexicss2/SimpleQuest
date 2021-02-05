package com.lex.simplequest.device.log

import android.util.Log
import com.lex.core.log.LogWatcher

class AndroidLogWatcher: LogWatcher {

    override fun logMessage(
        tag: String,
        logLevel: LogWatcher.LogLevel,
        message: String,
        throwable: Throwable?
    ) {
        when (logLevel) {
            LogWatcher.LogLevel.VERBOSE -> Log.v(tag, message, throwable)
            LogWatcher.LogLevel.DEBUG -> Log.d(tag, message, throwable)
            LogWatcher.LogLevel.INFO -> Log.i(tag, message, throwable)
            LogWatcher.LogLevel.WARNING -> Log.w(tag, message, throwable)
            LogWatcher.LogLevel.ERROR -> Log.e(tag, message, throwable)
        }
    }
}