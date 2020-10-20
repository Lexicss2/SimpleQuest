package com.lex.simplequest.presentation.base

import androidx.fragment.app.DialogFragment


abstract class BaseDialogFragment : DialogFragment() {

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