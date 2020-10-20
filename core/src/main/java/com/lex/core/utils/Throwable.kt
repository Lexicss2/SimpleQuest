package com.lex.core.utils

fun ignoreErrors(block: () -> Unit) =
    try {
        block()
    } catch (error: Throwable) {

    }

fun <R> ignoreErrors(defaultValue: R, block: () -> R): R =
    try {
        block()
    } catch (error: Throwable) {
        defaultValue
    }