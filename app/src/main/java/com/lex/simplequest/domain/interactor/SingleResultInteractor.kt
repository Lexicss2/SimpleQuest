package com.lex.simplequest.domain.interactor

import com.lex.core.utils.Cancellable

interface SingleResultInteractor<P : Any, R : Any> {

    fun execute(param: P, callback: Callback<R>): Cancellable

    interface Callback<R: Any> {
        fun onResult(result: R)
        fun onFailure(error: Throwable)
    }
}