package com.lex.simplequest.presentation.utils

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.IdRes
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.tabs.TabLayout


fun <T : View> Activity.bind(@IdRes res: Int): T {
    @Suppress("UNCHECKED_CAST")
    return findViewById(res)
}

fun <T : View> View.bind(@IdRes res: Int): T {
    @Suppress("UNCHECKED_CAST")
    return findViewById(res)
}

fun <T : View> Activity.bindOrNull(@IdRes res: Int): T? {
    @Suppress("UNCHECKED_CAST")
    return findViewById(res)
}

fun <T : View> View.bindOrNull(@IdRes res: Int): T? {
    @Suppress("UNCHECKED_CAST")
    return findViewById(res)
}

fun TabLayout.setTabsFont(fontPath: String) {
    (this.getChildAt(0) as? ViewGroup)?.let { tabs ->
        val tabsCount = tabs.childCount
        val typeface = Typeface.createFromAsset(this.context.assets, fontPath)
        (0 until tabsCount).forEach { tabIndex ->
            (tabs.getChildAt(tabIndex).getViewsByType(TextView::class.java)).forEach { textView ->
                textView.typeface = typeface
            }
        }
    }
}

fun CollapsingToolbarLayout.setCollapsedTitleTypeface(fontPath: String) {
    val typeface = Typeface.createFromAsset(this.context.assets, fontPath)
    this.setCollapsedTitleTypeface(typeface)
}

fun CollapsingToolbarLayout.setExpandedTitleTypeface(fontPath: String) {
    val typeface = Typeface.createFromAsset(this.context.assets, fontPath)
    this.setExpandedTitleTypeface(typeface)
}

fun <T : View> View.getViewsByType(clazz: Class<T>): List<T> =
    mutableListOf<T>().apply {
        if (this@getViewsByType is ViewGroup) {
            val childCount = this@getViewsByType.childCount
            for (i in 0 until childCount) {
                val child = this@getViewsByType.getChildAt(i)
                if (child is ViewGroup) {
                    addAll(child.getViewsByType(clazz))
                }
                if (clazz.isInstance(child)) {
                    add(clazz.cast(child)!!)
                }
            }
        }

    }.toList()

fun Int.dipToPx(): Int =
    (this * Resources.getSystem().displayMetrics.density).toInt()

fun PopupWindow.dimBehind() {
    val container = contentView.rootView
    val context = contentView.context
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val p = container.layoutParams as WindowManager.LayoutParams
    p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
    p.dimAmount = 0.3f
    wm.updateViewLayout(container, p)
}

fun TextView.mutateDrawables() {
    val drawables = compoundDrawablesRelative.map { it?.mutate() }
    background?.let { background = it.mutate() }
    setCompoundDrawablesRelative(drawables[0],drawables[1],drawables[2],drawables[3])
}

fun TextView.setDrawableTintColor(tintColor: Int) {
    for (drawable in this.compoundDrawablesRelative) {
        drawable?.setTint(tintColor)
    }
}

fun TextView.tintCompletely(tintColor: Int) {
    this.backgroundTintList = ColorStateList.valueOf(tintColor)
    this.setDrawableTintColor(tintColor)
    this.setTextColor(tintColor)
}
