package com.lex.simplequest.domain.interactor

import com.lex.core.utils.Cancellable


abstract class ImmediateSingleResultInteractor<P : Any, R : Any> : SingleResultInteractor<P, R> {

    override fun execute(param: P, callback: SingleResultInteractor.Callback<R>): Cancellable =
        Executor(createRunnable(param), callback)

    abstract fun createRunnable(param: P): () -> R

    private class Executor<R : Any>(
        runnable: () -> R,
        callback: SingleResultInteractor.Callback<R>
    ) : Cancellable {

        @Volatile
        private var isCancelled: Boolean = false

        init {
            try {
                val result = runnable.invoke()
                callback.onResult(result)
            } catch (error: Throwable) {
                callback.onFailure(error)
            }
        }

        override fun cancel() {
            isCancelled = true
        }

        override fun isCancelled(): Boolean =
            isCancelled
    }
}