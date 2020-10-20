package com.lex.simplequest.presentation.utils

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout

class ScrollingAppBarLayoutBehavior(context: Context, attrs: AttributeSet?) : AppBarLayout.Behavior(context, attrs) {

    var isScrollEnabled = true

    override fun onStartNestedScroll(
        parent: CoordinatorLayout,
        child: AppBarLayout,
        directTargetChild: View,
        target: View,
        nestedScrollAxes: Int,
        type: Int
    ) =
        isScrollEnabled

    override fun onTouchEvent(parent: CoordinatorLayout, child: AppBarLayout, ev: MotionEvent) =
        isScrollEnabled && super.onTouchEvent(parent, child, ev)

}