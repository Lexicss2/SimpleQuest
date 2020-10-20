package com.lex.simplequest.presentation.screen.launcher

import androidx.fragment.app.FragmentActivity
import com.lex.simplequest.presentation.screen.home.MainActivity

class LauncherRouterImpl : LauncherRouter {

    lateinit var activity: FragmentActivity

    override fun launchHomeScreen() {
        MainActivity.launch(activity, null, true)
    }

    override fun finishApplication() {
        activity.finish()
    }

    override fun goBack() {
        if (activity.supportFragmentManager.backStackEntryCount > 0) {
            activity.supportFragmentManager.popBackStack()
        } else {
            activity.finish()
        }
    }
}