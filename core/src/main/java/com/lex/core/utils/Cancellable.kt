package com.lex.core.utils


interface Cancellable {
    fun cancel()
    fun isCancelled(): Boolean
}