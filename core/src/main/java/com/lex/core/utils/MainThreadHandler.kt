package com.lex.core.utils

import android.os.Handler
import android.os.Looper

class MainThreadHandler {
    companion object {
        private val mainThreadHandler = Handler(Looper.getMainLooper())

        fun instance(): Handler {
            return mainThreadHandler
        }

        fun executeOnMainThread(runnable: Runnable) {
            if (Thread.currentThread() == Looper.getMainLooper().thread) {
                runnable.run()
            } else {
                mainThreadHandler.post(runnable)
            }
        }

        fun postOnMainThread(runnable: Runnable) {
            mainThreadHandler.post(runnable)
        }

        fun postOnMainThread(runnable: Runnable, delay: Long) {
            mainThreadHandler.postDelayed(runnable, delay)
        }

        fun isCalledOnMainThread(): Boolean {
            return Thread.currentThread() == Looper.getMainLooper().thread
        }

        /**
         * An easy way to ensure something is called on main thread. This method
         * generates RuntimeException if it's called not on main thread
         */
        fun checkCalledOnMainThread() {
            if (Thread.currentThread() != Looper.getMainLooper().thread) {
                throw RuntimeException("Get called not on main thread")
            }
        }

    }

    private fun MainThreadHandler() {}
}