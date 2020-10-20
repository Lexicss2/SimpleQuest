package com.lex.simplequest.presentation.base

import android.view.WindowManager
import androidx.fragment.app.Fragment


abstract class BaseFragment : Fragment(), HasToolbarData, ToolbarManager {

    override val toolbarData: ToolbarData?
        get() =
            if (shouldOverrideToolbar()) toolbarInfo
            else null

    protected val toolbarInfo = SettableToolbarData {
        updateToolbar()
    }

    override fun onStart() {
        super.onStart()
        getDesiredSoftInputMode()?.let { mode ->
            activity!!.window.setSoftInputMode(mode)
        }
    }

    override fun updateToolbar() {
        val toolbarManager = (parentFragment as? ToolbarManager) ?: (activity as? ToolbarManager)
        toolbarManager?.updateToolbar()
    }

    protected open fun shouldOverrideToolbar(): Boolean =
        true

    protected open fun getDesiredSoftInputMode(): Int? =
        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN

    /**
     * Return the "target" for this fragment of specified type. By default target is activity that owns
     * current fragment but also could be any fragment.
     *
     * @param clazz requested callback interface
     * @return requested callback or null if no callback of requested type is found
     */
    protected fun <T> getTarget(clazz: Class<T>): T? =
        arrayOf(targetFragment, parentFragment, activity)
            .firstOrNull { null != it && clazz.isInstance(it) }
            ?.let { target -> clazz.cast(target) }
}
