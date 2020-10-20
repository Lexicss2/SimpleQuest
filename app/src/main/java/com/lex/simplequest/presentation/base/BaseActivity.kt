package com.lex.simplequest.presentation.base

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.lex.simplequest.R
import com.lex.simplequest.presentation.utils.*
import com.lex.simplequest.presentation.utils.OneShotGlobalLayoutListener
import io.github.inflationx.viewpump.ViewPumpContextWrapper


abstract class BaseActivity : AppCompatActivity(), ToolbarManager, HasToolbarData {
    companion object {
        private const val THEME_ID = "themeId"
        private const val IS_THEME_CHANGED = "isThemeChanged"
    }

    private var toolbarOneShotGlobalLayoutListener: OneShotGlobalLayoutListener? = null
    override val toolbarData: ToolbarData?
        get() =
            if (shouldOverrideToolbar()) toolbarInfo
            else null
    protected val toolbarInfo = SettableToolbarData {
        if (shouldOverrideToolbar()) updateToolbar()
    }
    protected val topmostFragment: Fragment?
        get() = supportFragmentManager.getTopmostFragment(R.id.fragment_container)
    private var themeId: Int? = null
    private var isThemeChanged: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // apply selected theme
        getThemeId().let { themeId ->
            this.themeId = themeId
            this.isThemeChanged = false
            if (null != themeId) {
                setTheme(themeId)
            }
        }

        // start fade in animation if theme change was the cause of activity restart
        if (true == savedInstanceState?.getBoolean(IS_THEME_CHANGED, false)) {
            this.fadeIn()
        }

        supportFragmentManager.addOnBackStackChangedListener(fragmentBackStackChangedListener)
    }

    override fun onStart() {
        super.onStart()
        recreateIfThemeChanged()
        bindOrNull<View>(android.R.id.content)
            ?.viewTreeObserver
            ?.addOnGlobalFocusChangeListener(globalFocusChangedListener)
        onFragmentBackStackChanged()
    }

    override fun onStop() {
        super.onStop()
        bindOrNull<View>(android.R.id.content)
            ?.viewTreeObserver
            ?.removeOnGlobalFocusChangeListener(globalFocusChangedListener)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        themeId?.let { outState.putInt(THEME_ID, it) }
        outState.putBoolean(IS_THEME_CHANGED, isThemeChanged)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var handled = false
        if (android.R.id.home == item.itemId) {
            handled = handleBackPressed()
        }
        return if (handled) true else super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (!handleBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun setSupportActionBar(toolbar: Toolbar?) {
        super.setSupportActionBar(toolbar)

        // calligraphy library works incorrect with toolbar's subtitle, so we need to update it one more time
        toolbarOneShotGlobalLayoutListener?.close()
        toolbarOneShotGlobalLayoutListener = toolbar?.let {
            OneShotGlobalLayoutListener(
                it,
                ViewTreeObserver.OnGlobalLayoutListener { updateToolbar() })
        }
    }

    override fun updateToolbar() {
        supportActionBar?.let { actionBar ->
            val toolbarData =
                topmostFragment?.let { if (it is HasToolbarData) it.toolbarData else null } ?: this.toolbarData
            if (null != toolbarData) {
                actionBar.title = toolbarData.title
                actionBar.subtitle = toolbarData.subtitle
            } else {
                actionBar.title = null
                actionBar.subtitle = null
            }
        }
    }

    protected open fun shouldOverrideToolbar(): Boolean =
        true

    protected open fun handleBackPressed(): Boolean {
        val fragments = supportFragmentManager.fragments
        if (fragments.size > 0) {
            fragments.forEach {
                if (it is BackPressInterceptor && it.isVisible && it.onBackPressed()) {
                    return true
                }
            }
        }
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
        } else {
            finish()
        }
        return true
    }

    protected open fun onFragmentBackStackChanged() {
        setupActionBar()
        updateToolbar()
        hideKeyboard(false)
    }

    protected fun hideKeyboard(immediately: Boolean) {
        if (immediately) {
            hideKeyboard(this@BaseActivity, null)
        } else {
            window.decorView.rootView.post { hideKeyboard(this@BaseActivity, null) }
        }
    }

    protected fun recreateIfThemeChanged() {
        getThemeId().let { themeId ->
            if (this.themeId != themeId) {
                this.themeId = themeId
                this.isThemeChanged = true
                this.fadeOutAndRecreate()
            }
        }
    }

    protected open fun getThemeId(): Int? =
        null

    private fun setupActionBar() {
        val toolbar = this.topmostFragment?.view?.bindOrNull(R.id.toolbar) ?: bindOrNull<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        invalidateOptionsMenu()
    }

    private val fragmentBackStackChangedListener = FragmentManager.OnBackStackChangedListener {
        onFragmentBackStackChanged()
    }

    private val globalFocusChangedListener =
        ViewTreeObserver.OnGlobalFocusChangeListener { _, newFocus ->
            if (newFocus !is EditText || InputType.TYPE_NULL == newFocus.inputType) {
                hideKeyboard(this@BaseActivity, newFocus)
            }
        }
}