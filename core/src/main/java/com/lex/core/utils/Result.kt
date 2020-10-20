package com.lex.core.utils

sealed class Result<out T> {
    data class Error(val error: Throwable) : Result<Nothing>()
    data class Success<T>(val value: T) : Result<T>()
}
