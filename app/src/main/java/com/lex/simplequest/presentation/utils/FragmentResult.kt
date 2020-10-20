package com.lex.simplequest.presentation.utils

import android.os.Bundle

data class FragmentResult(val resultCode: Int, val data: Bundle)

object FragmentResultCode {
    const val RESULT_CANCELED = 0
    const val RESULT_OK = -1
}

object FragmentResults {

    private val results = mutableMapOf<String, FragmentResult>()

    fun put(tag: String, fragmentResult: FragmentResult) {
        results[tag] = fragmentResult
    }

    fun remove(tag: String): FragmentResult? =
        results.remove(tag)

    fun contains(tag: String): Boolean =
        results.contains(tag)
}