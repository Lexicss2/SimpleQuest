package com.lex.simplequest.presentation.utils

import android.net.Uri
import java.lang.NullPointerException
import java.net.MalformedURLException

fun String.urlHost(defaultValue: String = ""): String =
    try {
        Uri.parse(this@urlHost).host!!
    } catch (error: MalformedURLException) {
        defaultValue
    } catch(error: NullPointerException) {
        defaultValue
    }