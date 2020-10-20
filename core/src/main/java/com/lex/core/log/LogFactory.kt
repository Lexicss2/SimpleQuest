package com.lex.core.log


interface LogFactory {
    fun get(tag: String): Log
    fun addLogWatcher(logWatcher: LogWatcher)
    fun removeLogWatcher(logWatcher: LogWatcher)
}