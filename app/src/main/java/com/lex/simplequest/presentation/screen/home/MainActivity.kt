package com.lex.simplequest.presentation.screen.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.lex.simplequest.R
import com.lex.simplequest.presentation.base.BaseMvpActivity
import com.lex.simplequest.presentation.base.ToolbarBackButtonOverride
import com.lex.simplequest.presentation.screen.home.home.HomeFragment
import com.lex.simplequest.presentation.screen.home.map.MapFragment
import com.lex.simplequest.presentation.screen.home.settings.SettingsFragment
import com.lex.simplequest.presentation.screen.home.tracks.TracksFragment
import com.lex.simplequest.presentation.utils.bind
import com.softeq.android.mvp.PresenterStateHolder
import com.softeq.android.mvp.VoidPresenterStateHolder
import java.lang.IllegalStateException

class MainActivity(private val router: MainRouterImpl = MainRouterImpl()) :
    BaseMvpActivity<MainActivityContract.Ui, MainActivityContract.Presenter.State, MainActivityContract.Presenter>(),
    MainActivityContract.Ui, MainRouter by router {

    companion object {
        fun launch(context: Context, deepLink: String?, clearTask: Boolean) {
            val intent = Intent(context, MainActivity::class.java).apply {
                if (clearTask) {
                    addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                    )
                } else {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    )
                }
            }
            context.startActivity(intent)
        }
    }

    private lateinit var bottomNavigationView: BottomNavigationView
    private var isSettlingBottomNavigation = false

    private val bottomNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
            if (!isSettlingBottomNavigation) {
                if (bottomNavigationView.selectedItemId != menuItem.itemId) {
                    when (menuItem.itemId) {
                        R.id.action_navigation_home -> {
                            presenter.onNavigationHomeClicked()
                            true
                        }

                        R.id.action_navigation_map -> {
                            presenter.onNavigationMapClicked()
                            true
                        }

                        R.id.action_navigation_track_list -> {
                            presenter.onNavigationTrackListClicked()
                            true
                        }

                        R.id.action_navigation_settings -> {
                            presenter.onNavigationSettingsClicked()
                            true
                        }

                        else -> throw IllegalStateException("Unknown navigation action")
                    }
                } else true
            } else true
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        router.activity = this
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottomNavigationView = bind<BottomNavigationView>(R.id.bottom_navigation_view).apply {
            setOnNavigationItemSelectedListener(bottomNavigationItemSelectedListener)
        }

        if (null == savedInstanceState) {
            router.showHome()
        }
    }

    override fun onStart() {
        super.onStart()
        updateBottomNavigationView()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }

    override fun onFragmentBackStackChanged() {
        super.onFragmentBackStackChanged()
        updateBottomNavigationView()
    }

    override fun updateToolbar() {
        super.updateToolbar()
        supportActionBar?.let { actionBar ->
            val backStackSize = supportFragmentManager.backStackEntryCount
            if (backStackSize > 1) {
                val toolbarBackButtonDrawable =
                    (topmostFragment as? ToolbarBackButtonOverride)?.getToolbarBackButtonDrawable()
                        ?: ContextCompat.getDrawable(this, R.drawable.ic_toolbar_back)
                actionBar.setHomeAsUpIndicator(toolbarBackButtonDrawable)
                actionBar.setHomeButtonEnabled(true)
                actionBar.setDisplayHomeAsUpEnabled(true)
            } else {
                actionBar.setHomeAsUpIndicator(null)
                actionBar.setHomeButtonEnabled(false)
                actionBar.setDisplayHomeAsUpEnabled(false)
            }
        }
    }

    private fun updateBottomNavigationView() {
        val backStackEntryCount = supportFragmentManager.backStackEntryCount
        val selectedBottomNavigationItemId = if (1 == backStackEntryCount) {
            when (topmostFragment) {
                is HomeFragment -> R.id.action_navigation_home
                is MapFragment -> R.id.action_navigation_map
                is TracksFragment -> R.id.action_navigation_track_list
                is SettingsFragment -> R.id.action_navigation_settings
                else -> -1
            }
            -1
        } else bottomNavigationView.selectedItemId
        isSettlingBottomNavigation = true
        bottomNavigationView.selectedItemId = selectedBottomNavigationItemId
        isSettlingBottomNavigation = false
        bottomNavigationView.visibility =
            if (1 == backStackEntryCount) View.VISIBLE
            else View.GONE
    }

    override fun createPresenterStateHolder(): PresenterStateHolder<MainActivityContract.Presenter.State> =
        VoidPresenterStateHolder()

    override fun getUi(): MainActivityContract.Ui =
        this

    override fun createPresenter(): MainActivityContract.Presenter =
        MainActivityPresenter(this)
}