package com.lex.simplequest.domain.interactor

import com.lex.core.utils.Cancellable
import io.reactivex.Observable
import io.reactivex.observers.DisposableObserver


abstract class RxMultiResultInteractor<P : Any, R : Any> : MultiResultInteractor<P, R> {

    override fun execute(param: P, callback: MultiResultInteractor.Callback<R>): Cancellable =
        Executor(createObservable(param), callback)

    abstract fun createObservable(param: P): Observable<R>

    private class Executor<R : Any>(
        observable: Observable<R>,
        private val callback: MultiResultInteractor.Callback<R>
    ) : Cancellable {

        private val disposable = observable.subscribeWith(object : DisposableObserver<R>() {

            override fun onNext(result: R) {
                callback.onResult(result)
            }

            override fun onComplete() {
                callback.onComplete()
            }

            override fun onError(error: Throwable) {
                callback.onFailure(error)
            }
        })

        override fun cancel() {
            disposable.dispose()
        }

        override fun isCancelled(): Boolean =
            disposable.isDisposed
    }
}