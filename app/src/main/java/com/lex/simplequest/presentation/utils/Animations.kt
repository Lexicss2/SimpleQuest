package com.lex.simplequest.presentation.utils

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

internal fun createFadeInAnimator(rootView: View, duration: Long): Animator =
    ObjectAnimator.ofFloat(rootView, "alpha", 0f, 1f).apply {
        this.duration = duration
        this.interpolator = AccelerateDecelerateInterpolator()
    }

internal fun createFadeOutAnimator(rootView: View, duration: Long): Animator =
    ObjectAnimator.ofFloat(rootView, "alpha", rootView.alpha, 0f).apply {
        this.duration = duration
        this.interpolator = AccelerateDecelerateInterpolator()
    }
