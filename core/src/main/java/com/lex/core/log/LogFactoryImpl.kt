package com.lex.core.log

import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet


class LogFactoryImpl : LogFactory {

    private val loggers = HashMap<String, WeakReference<Log>>()
    private val watchers = CopyOnWriteArraySet<LogWatcher>()
    private val logSync = Any()

    override fun get(tag: String): Log =
        synchronized(this) {
            var log: Log? = loggers[tag]?.get()
            if (null == log) {
                log = LogImpl(tag)
                loggers.put(tag, WeakReference(log))
            }
            log
        }

    override fun addLogWatcher(logWatcher: LogWatcher) {
        watchers.add(logWatcher)
    }

    override fun removeLogWatcher(logWatcher: LogWatcher) {
        watchers.remove(logWatcher)
    }

    private fun handleLogMessage(tag: String, logLevel: LogWatcher.LogLevel, message: String, throwable: Throwable?) =
        synchronized(logSync) {
            watchers.forEach { it.logMessage(tag, logLevel, message, throwable) }
        }

    inner class LogImpl(private val tag: String) : Log {

        override fun v(msg: String) {
            handleLogMessage(tag, LogWatcher.LogLevel.VERBOSE, msg, null)
        }

        override fun v(throwable: Throwable?, msg: String) {
            handleLogMessage(tag, LogWatcher.LogLevel.VERBOSE, msg, throwable)
        }

        override fun d(msg: String) {
            handleLogMessage(tag, LogWatcher.LogLevel.DEBUG, msg, null)
        }

        override fun d(throwable: Throwable?, msg: String) {
            handleLogMessage(tag, LogWatcher.LogLevel.DEBUG, msg, throwable)
        }

        override fun i(msg: String) {
            handleLogMessage(tag, LogWatcher.LogLevel.INFO, msg, null)
        }

        override fun i(throwable: Throwable?, msg: String) {
            handleLogMessage(tag, LogWatcher.LogLevel.INFO, msg, throwable)
        }

        override fun w(msg: String) {
            handleLogMessage(tag, LogWatcher.LogLevel.WARNING, msg, null)
        }

        override fun w(throwable: Throwable?, msg: String) {
            handleLogMessage(tag, LogWatcher.LogLevel.WARNING, msg, throwable)
        }

        override fun e(msg: String) {
            handleLogMessage(tag, LogWatcher.LogLevel.ERROR, msg, null)
        }

        override fun e(throwable: Throwable?, msg: String) {
            handleLogMessage(tag, LogWatcher.LogLevel.ERROR, msg, throwable)
        }
    }
}