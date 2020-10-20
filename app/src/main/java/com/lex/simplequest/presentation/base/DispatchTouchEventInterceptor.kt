package com.lex.simplequest.presentation.base

import android.app.Activity
import android.view.MotionEvent

interface DispatchTouchEventInterceptor {
    fun dispatchTouchEvent(activity: Activity, event: MotionEvent): Boolean?
}