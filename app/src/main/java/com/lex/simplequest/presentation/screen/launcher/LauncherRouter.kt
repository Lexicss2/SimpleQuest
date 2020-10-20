package com.lex.simplequest.presentation.screen.launcher

import com.lex.simplequest.presentation.base.Router

interface LauncherRouter : Router {
    fun launchHomeScreen()
    fun finishApplication()
}