package com.lex.simplequest.domain.utils

import com.lex.core.utils.ObservableValue
import io.reactivex.Observable
import io.reactivex.disposables.Disposables

fun <T> ObservableValue<T>.asRxObservable(): Observable<T> =
    Observable.create { emitter ->
        val valueObserver = object : ObservableValue.ValueObserver<T> {
            override fun onChanged(value: T) {
                if (!emitter.isDisposed) {
                    emitter.onNext(value)
                }
            }
        }
        if (!emitter.isDisposed) {
            emitter.setDisposable(Disposables.fromAction { this@asRxObservable.removeObserver(valueObserver) })
            this@asRxObservable.addObserver(valueObserver, true)
        }
    }