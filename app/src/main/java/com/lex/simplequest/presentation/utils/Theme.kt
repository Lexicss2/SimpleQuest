package com.lex.simplequest.presentation.utils

import android.content.Context
import android.util.TypedValue

fun Context.getThemeColor(attr: Int): Int =
    TypedValue().let { typedValue ->
        this@getThemeColor.theme.resolveAttribute(attr, typedValue, true)
        typedValue.data
    }

fun Context.getThemeResourceId(attr: Int): Int? =
    TypedValue().let { typedValue ->
        this@getThemeResourceId.theme.resolveAttribute(attr, typedValue, true)
        typedValue.resourceId
    }
