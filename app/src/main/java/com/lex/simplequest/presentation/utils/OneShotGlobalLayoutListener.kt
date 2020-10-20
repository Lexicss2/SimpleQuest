package com.lex.simplequest.presentation.utils

import android.view.View
import android.view.ViewTreeObserver
import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean

class OneShotGlobalLayoutListener(
    private val view: View,
    private val listener: ViewTreeObserver.OnGlobalLayoutListener
) : Closeable {

    private val isClosed = AtomicBoolean(false)
    private val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        listener.onGlobalLayout()
        cleanup()
    }

    init {
        view.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }

    private fun cleanup() {
        val viewTreeObserver = view.viewTreeObserver
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
        }
    }

    override fun close() {
        if (!isClosed.getAndSet(true)) {
            cleanup()
        }
    }
}