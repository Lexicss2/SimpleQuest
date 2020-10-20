package com.lex.core.log


interface LogWatcher {
    fun logMessage(tag: String, logLevel: LogLevel, message: String, throwable: Throwable?)

    enum class LogLevel {
        VERBOSE,
        DEBUG,
        INFO,
        WARNING,
        ERROR
    }
}