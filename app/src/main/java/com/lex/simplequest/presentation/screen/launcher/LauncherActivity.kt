package com.lex.simplequest.presentation.screen.launcher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.lex.simplequest.R
import com.lex.simplequest.presentation.base.BaseMvpActivity
import com.lex.simplequest.presentation.base.ToolbarBackButtonOverride
import com.lex.simplequest.presentation.screen.launcher.initialization.InitializationFragment
import com.lex.simplequest.presentation.utils.inTransaction
import com.softeq.android.mvp.PresenterStateHolder
import com.softeq.android.mvp.VoidPresenterStateHolder

class LauncherActivity(private val router: LauncherRouterImpl = LauncherRouterImpl()) :
    BaseMvpActivity<LauncherActivityContract.Ui, LauncherActivityContract.Presenter.State, LauncherActivityContract.Presenter>(),
    LauncherActivityContract.Ui,
    LauncherRouter by router {

    companion object {
        fun launch(context: Context, deepLink: String?) {
            val intent = Intent(context, LauncherActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        router.activity = this
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        if (null == savedInstanceState) {
            supportFragmentManager.inTransaction {
                val fragment = InitializationFragment.newInstance()
                replace(R.id.fragment_container, fragment, null)
                addToBackStack(fragment.javaClass.name)
            }
        }
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

    override fun getUi(): LauncherActivityContract.Ui =
        this

    override fun createPresenter(): LauncherActivityContract.Presenter =
        LauncherActivityPresenter(this)

    override fun createPresenterStateHolder(): PresenterStateHolder<LauncherActivityContract.Presenter.State> =
        VoidPresenterStateHolder()
}