package com.lex.simplequest.presentation.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.Window

private const val ANIMATION_DURATION = 250L

fun Activity.fadeOutAndRecreate() {
    val rootView = (this.findViewById(Window.ID_ANDROID_CONTENT) as ViewGroup).getChildAt(0)
    val animator = createFadeOutAnimator(rootView, ANIMATION_DURATION)
    animator.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            rootView.visibility = View.INVISIBLE
            this@fadeOutAndRecreate.recreate()
        }
    })
    animator.start()
}

fun Activity.fadeIn() {
    Runnable {
        val rootView = (this@fadeIn.findViewById(Window.ID_ANDROID_CONTENT) as ViewGroup).getChildAt(0)
        val animator = createFadeInAnimator(rootView, ANIMATION_DURATION)
        animator.start()
    }.postOnMainThread()
}
