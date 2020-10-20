package com.lex.core.utils

abstract class Optional<out T : Any?> {

    companion object {
        fun <T> of(element: T): Optional<T> =
            OptionalValue(element)

        fun <T> absent(): Optional<T> =
            OptionalAbsent()

        fun <T> fromNullable(value: T?): Optional<T> =
            if (null != value) OptionalValue(value)
            else OptionalAbsent()
    }

    abstract val absent: Boolean

    val present: Boolean =
        !absent

    abstract fun get(): T
}

private class OptionalValue<out T : Any?>(private val value: T) : Optional<T>() {

    override val absent: Boolean
        get() = false

    override fun get(): T =
        value
}

private class OptionalAbsent<out T : Any?> : Optional<T>() {

    override val absent: Boolean
        get() = true

    override fun get(): T =
        throw NoSuchElementException("No value present")
}

