package com.lex.simplequest.presentation.utils

import android.app.Activity
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

fun hideKeyboard(activity: Activity, view: View?) {
    val v: View? = view ?: activity.currentFocus ?: View(activity)
    hideKeyboard(activity.applicationContext, v)
}

fun hideKeyboard(context: Context, view: View?) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    view?.also { v ->
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }
}

fun showKeyboard(context: Context, view: View?) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    view?.also { v ->
        v.requestFocus()
        imm.showSoftInput(view, 0)
    }
}

fun hideKeyboardOnTouchOutsideEditText(activity: Activity, event: MotionEvent): Boolean =
    if (MotionEvent.ACTION_DOWN == event.action) {
        val v = activity.currentFocus
        if (v is EditText) {
            val location = IntArray(2)
            v.getLocationOnScreen(location)
            val x = event.rawX + v.getLeft() - location[0]
            val y = event.rawY + v.getTop() - location[1]
            if (x < v.getLeft() || x >= v.getRight() || y < v.getTop() || y > v.getBottom()) {
                hideKeyboard(activity, null)
                true
            } else false
        } else false
    } else false