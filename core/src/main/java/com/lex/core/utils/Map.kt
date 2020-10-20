package com.lex.core.utils

fun <K, V, KK, VV> Map<K, V>.transform(transofrmer: (Map.Entry<K, V>) -> Pair<KK, VV>): Map<KK, VV> =
    mutableMapOf<KK, VV>().apply {
        this@transform.entries.forEach { entry ->
            val (kk, vv) = transofrmer(entry)
            this[kk] = vv
        }
    }.toMap()