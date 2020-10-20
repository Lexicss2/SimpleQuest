package com.lex.core.log


interface Log {
    fun v(msg: String)
    fun v(throwable: Throwable?, msg: String)
    fun d(msg: String)
    fun d(throwable: Throwable?, msg: String)
    fun i(msg: String)
    fun i(throwable: Throwable?, msg: String)
    fun w(msg: String)
    fun w(throwable: Throwable?, msg: String)
    fun e(msg: String)
    fun e(throwable: Throwable?, msg: String)
}