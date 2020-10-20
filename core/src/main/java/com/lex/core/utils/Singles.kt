package com.lex.core.utils


import io.reactivex.Single
import io.reactivex.functions.*


object Singles {

    inline fun <T1, T2, R> zip(source1: Single<T1>, source2: Single<T2>, crossinline combineFunction: (T1, T2) -> R) =
        Single.zip(source1, source2,
            BiFunction<T1, T2, R> { t1, t2 -> combineFunction(t1, t2) })!!

    inline fun <T1, T2, T3, R> zip(
        source1: Single<T1>,
        source2: Single<T2>,
        source3: Single<T3>,
        crossinline combineFunction: (T1, T2, T3) -> R
    ) =
        Single.zip(source1, source2, source3,
            Function3<T1, T2, T3, R> { t1, t2, t3 -> combineFunction(t1, t2, t3) })

    inline fun <T1, T2, T3, T4, R> zip(
        source1: Single<T1>,
        source2: Single<T2>,
        source3: Single<T3>,
        source4: Single<T4>,
        crossinline combineFunction: (T1, T2, T3, T4) -> R
    ) =
        Single.zip(source1, source2, source3, source4,
            Function4<T1, T2, T3, T4, R> { t1, t2, t3, t4 -> combineFunction(t1, t2, t3, t4) })

    inline fun <T1, T2, T3, T4, T5, R> zip(
        source1: Single<T1>,
        source2: Single<T2>,
        source3: Single<T3>,
        source4: Single<T4>,
        source5: Single<T5>,
        crossinline combineFunction: (T1, T2, T3, T4, T5) -> R
    ) =
        Single.zip(source1, source2, source3, source4, source5,
            Function5<T1, T2, T3, T4, T5, R> { t1, t2, t3, t4, t5 -> combineFunction(t1, t2, t3, t4, t5) })

    inline fun <T1, T2, T3, T4, T5, T6, R> zip(
        source1: Single<T1>,
        source2: Single<T2>,
        source3: Single<T3>,
        source4: Single<T4>,
        source5: Single<T5>,
        source6: Single<T6>,
        crossinline combineFunction: (T1, T2, T3, T4, T5, T6) -> R
    ) =
        Single.zip(source1, source2, source3, source4, source5, source6,
            Function6<T1, T2, T3, T4, T5, T6, R> { t1, t2, t3, t4, t5, t6 -> combineFunction(t1, t2, t3, t4, t5, t6) })

    inline fun <T1, T2, T3, T4, T5, T6, T7, R> zip(
        source1: Single<T1>,
        source2: Single<T2>,
        source3: Single<T3>,
        source4: Single<T4>,
        source5: Single<T5>,
        source6: Single<T6>,
        source7: Single<T7>,
        crossinline combineFunction: (T1, T2, T3, T4, T5, T6, T7) -> R
    ) =
        Single.zip(source1, source2, source3, source4, source5, source6, source7,
            Function7<T1, T2, T3, T4, T5, T6, T7, R> { t1, t2, t3, t4, t5, t6, t7 ->
                combineFunction(
                    t1,
                    t2,
                    t3,
                    t4,
                    t5,
                    t6,
                    t7
                )
            })
}
