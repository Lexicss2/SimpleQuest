package com.lex.simplequest.presentation.screen.home

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.lex.simplequest.R
import com.lex.simplequest.presentation.screen.home.home.HomeFragment
import com.lex.simplequest.presentation.screen.home.map.MapFragment
import com.lex.simplequest.presentation.screen.home.settings.SettingsFragment
import com.lex.simplequest.presentation.screen.home.tracks.TracksFragment
import com.lex.simplequest.presentation.utils.clearBackStack
import com.lex.simplequest.presentation.utils.inTransaction

class MainRouterImpl : MainRouter {
    lateinit var activity: FragmentActivity

    override fun showHome() {
        addFragment(HomeFragment.newInstance(), true)
    }

    override fun showMap() {
        addFragment(MapFragment.newInstance(), true)
//        val intent = Intent(activity, MapsActivity::class.java)
//        activity.startActivity(intent)
    }

    override fun showTracks() {
        addFragment(TracksFragment.newInstance(), true)
    }

    override fun showSettings() {
        addFragment(SettingsFragment.newInstance(), true)
    }

    override fun goBack() {
        if (activity.supportFragmentManager.backStackEntryCount > 1) {
            activity.supportFragmentManager.popBackStack()
        } else {
            activity.finish()
        }
    }

    private fun addFragment(fragment: Fragment, clearBackStack: Boolean) {
        if (clearBackStack) activity.supportFragmentManager.clearBackStack()
        activity.supportFragmentManager.inTransaction {
            replace(R.id.fragment_container, fragment, null)
            addToBackStack(fragment.javaClass.name)
        }
    }

    private fun addFragments(vararg fragments: Fragment, clearBackStack: Boolean = false) {
        if (clearBackStack) activity.supportFragmentManager.clearBackStack()
        fragments.forEach { fragment ->
            activity.supportFragmentManager.inTransaction {
                replace(R.id.fragment_container, fragment, null)
                addToBackStack(fragment.javaClass.name)
            }
        }
    }
}