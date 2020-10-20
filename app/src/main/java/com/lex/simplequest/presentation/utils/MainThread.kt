package com.lex.simplequest.presentation.utils

import android.os.Handler
import android.os.Looper


object MainThread {

    val handler: Handler = Handler(Looper.getMainLooper())

    val isCalledOnMainThread: Boolean
        get() {
            if (Thread.currentThread() !== Looper.getMainLooper().thread) {
                return false
            }
            return true
        }

    val checkCalledOnMainThread: Boolean
        get() {
            if (Thread.currentThread() !== Looper.getMainLooper().thread) {
                throw RuntimeException("Get called outside of main thread")
            }
            return true
        }
}

fun Runnable.postOnMainThread() =
    MainThread.handler.post(this)

fun Runnable.postOnMainThread(delay: Long) =
    MainThread.handler.postDelayed(this, delay)

fun Runnable.removeFromMainThread() =
    MainThread.handler.removeCallbacks(this)

fun Runnable.executeOnMainThread() {
    if (Thread.currentThread() === Looper.getMainLooper().thread) {
        this.run()
    } else {
        MainThread.handler.post(this)
    }
}