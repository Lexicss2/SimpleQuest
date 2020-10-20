package com.lex.simplequest.presentation.utils

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.lex.simplequest.R

fun View.setPropertyName(@IdRes prop: Int, @StringRes name: Int) {
    (bind<View>(prop)).setText(R.id.property_name, name)
}

fun View.setPropertyName(@IdRes prop: Int, name: String?) {
    bind<View>(prop).setText(R.id.property_name, name)
}

fun View.setPropertyName(@StringRes name: Int) {
    setText(R.id.property_name, name)
}

fun View.setPropertyName(name: String?) {
    setText(R.id.property_name, name)
}

fun View.setPropertyHint(@IdRes prop: Int, @StringRes name: Int) {
    (bind<View>(prop)).setText(R.id.property_hint, name)
}

fun View.setPropertyHint(@IdRes prop: Int, name: String?) {
    bind<View>(prop).setText(R.id.property_hint, name)
}

fun View.setPropertyHint(@StringRes name: Int) {
    setText(R.id.property_hint, name)
}

fun View.setPropertyHint(name: String?) {
    setText(R.id.property_hint, name)
}

fun View.setPropertyValue(@IdRes prop: Int, @StringRes value: Int) {
    (bind<View>(prop)).setText(R.id.property_value, value)
}

fun View.setPropertyValue(@IdRes prop: Int, value: String?) {
    bind<View>(prop).setText(R.id.property_value, value)
}

fun View.setPropertyValue(@StringRes value: Int) {
    setText(R.id.property_value, value)
}

fun View.setPropertyValue(value: String?) {
    setText(R.id.property_value, value)
}

fun View.setPropertyIcon(@IdRes prop: Int, @DrawableRes icon: Int) {
    (bind<View>(prop)).setIcon(R.id.property_icon, icon)
}

fun View.setPropertyIcon(@IdRes prop: Int, icon: Drawable) {
    bind<View>(prop).setIcon(R.id.property_icon, icon)
}

fun View.setPropertyIcon(@DrawableRes icon: Int) {
    setIcon(R.id.property_icon, icon)
}

fun View.setPropertyIcon(icon: Drawable) {
    setIcon(R.id.property_icon, icon)
}

fun View.setPropertyValueSecondary(@IdRes prop: Int, @StringRes value: Int) {
    (bind<View>(prop)).setText(R.id.property_value_secondary, value)
}

fun View.setPropertyValueSecondary(@IdRes prop: Int, value: String?) {
    (bind<View>(prop)).setText(R.id.property_value_secondary, value)
}

fun View.setPropertyVisibility(@IdRes prop: Int, @Visibility visibility: Int) {
    (bind<View>(prop)).visibility = visibility
}

fun View.setPropertyIconVisibility(@IdRes prop: Int, @Visibility visibility: Int) {
    (bind<View>(prop)).bind<View>(R.id.property_icon).visibility = visibility
}

fun View.setPropertyValueColor(@IdRes prop: Int, @ColorInt color: Int) {
    (bind<View>(prop)).setTextColor(R.id.property_value, color)
}

fun View.setPropertyValueColor(@IdRes prop: Int, color: ColorStateList) {
    (bind<View>(prop)).setTextColor(R.id.property_value, color)
}

private fun View.setText(@IdRes id: Int, @StringRes text: Int) {
    (bind(id) as TextView).setText(text)
}

private fun View.setText(@IdRes id: Int, text: String?) {
    (bind(id) as TextView).text = text
}

private fun View.setTextColor(@IdRes id: Int, @ColorInt textColor: Int) {
    (bind(id) as TextView).setTextColor(textColor)
}

private fun View.setTextColor(@IdRes id: Int, textColor: ColorStateList) {
    (bind(id) as TextView).setTextColor(textColor)
}

private fun View.setIcon(@IdRes id: Int, drawableRes: Int) {
    setIcon(id, ContextCompat.getDrawable(this.context, drawableRes)!!)
}

private fun View.setIcon(@IdRes id: Int, drawable: Drawable) {
    when (val view = bind<View>(id)) {
        is ImageView -> {
            view.setImageDrawable(drawable)
        }
        is TextView -> {
            view.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null)
        }
        else -> throw IllegalArgumentException("Can't set image")
    }
}

