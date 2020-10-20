package com.lex.simplequest.presentation.utils

import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.TextView

class CompoundDrawableClickListener(
    private val textView: TextView,
    private val clickListener: OnCompoundDrawableClickListener
) : View.OnTouchListener {

    companion object {
        const val DRAWABLE_LEFT = 0
        const val DRAWABLE_TOP = 1
        const val DRAWABLE_RIGHT = 2
        const val DRAWABLE_BOTTOM = 3
        const val DRAWABLE_UNKNOWN = -1

        private fun distance(x1: Int, y1: Int, x2: Int, y2: Int): Int {
            val deltaX = x2 - x1
            val deltaY = y2 - y1
            return Math.sqrt((deltaX * deltaX + deltaY * deltaY).toDouble()).toInt()
        }
    }

    private val touchSlope: Int = ViewConfiguration.get(textView.context).scaledTouchSlop
    private val tempRect = Rect()
    private var touchX: Int = 0
    private var touchY: Int = 0
    private var touchedDrawableType = DRAWABLE_UNKNOWN

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {
                touchX = event.rawX.toInt()
                touchY = event.rawY.toInt()
                touchedDrawableType = getDrawableType(textView, touchX, touchY, tempRect)
            }

            MotionEvent.ACTION_MOVE -> if (DRAWABLE_UNKNOWN != touchedDrawableType) {
                if (distance(event.rawX.toInt(), event.rawY.toInt(), touchX, touchY) > touchSlope) {
                    touchedDrawableType = DRAWABLE_UNKNOWN
                }
            }

            MotionEvent.ACTION_UP -> {
                if (DRAWABLE_UNKNOWN != touchedDrawableType) {
                    clickListener.onClick(touchedDrawableType)
                    touchedDrawableType = DRAWABLE_UNKNOWN
                    return true
                }
                touchedDrawableType = DRAWABLE_UNKNOWN
            }

            MotionEvent.ACTION_CANCEL -> touchedDrawableType = DRAWABLE_UNKNOWN
        }
        return DRAWABLE_UNKNOWN != touchedDrawableType
    }

    private fun getDrawableType(textView: TextView, x: Int, y: Int, tempRect: Rect): Int {
        val locOnScreen = IntArray(2)
        textView.getLocationInWindow(locOnScreen)
        val left = locOnScreen[0]
        val top = locOnScreen[1]
        val right = left + textView.width
        val bottom = top + textView.height
        val drawables = textView.compoundDrawables
        if (drawables.isNotEmpty()) {
            if (null != drawables[0]) {
                tempRect.set(
                    left + textView.paddingStart,
                    top + textView.paddingTop,
                    left + textView.paddingStart + drawables[0].bounds.width(),
                    bottom - textView.paddingBottom
                )
                if (tempRect.contains(x, y)) return DRAWABLE_LEFT
            }
            if (null != drawables[1]) {
                tempRect.set(
                    left + textView.paddingStart,
                    top + textView.paddingTop,
                    right - textView.paddingEnd,
                    top + textView.paddingTop + drawables[1].bounds.height()
                )
                if (tempRect.contains(x, y)) return DRAWABLE_TOP
            }
            if (null != drawables[2]) {
                tempRect.set(
                    right - textView.paddingEnd - drawables[2].bounds.width(),
                    top + textView.paddingTop,
                    right - textView.paddingEnd,
                    bottom - textView.paddingBottom
                )
                if (tempRect.contains(x, y)) return DRAWABLE_RIGHT
            }
            if (null != drawables[3]) {
                tempRect.set(
                    left + textView.paddingStart,
                    bottom - textView.paddingBottom - drawables[3].bounds.height(),
                    right - textView.paddingEnd,
                    bottom - textView.paddingBottom
                )
                if (tempRect.contains(x, y)) return DRAWABLE_BOTTOM
            }
        }
        return DRAWABLE_UNKNOWN
    }

    interface OnCompoundDrawableClickListener {
        fun onClick(drawable: Int)
    }
}