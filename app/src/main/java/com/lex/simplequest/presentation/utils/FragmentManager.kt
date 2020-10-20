package com.lex.simplequest.presentation.utils

import androidx.annotation.IdRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.lex.simplequest.R


fun FragmentManager.clearBackStack() {
    executePendingTransactions()
    val backStackCount = backStackEntryCount
    for (i in 0 until backStackCount) {
        popBackStack(getBackStackEntryAt(i).id, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }
}

fun FragmentManager.clearBackStackImmediate() {
    val backStackCount = backStackEntryCount
    for (i in 0 until backStackCount) {
        popBackStack(getBackStackEntryAt(i).id, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }
    executePendingTransactions()
}

inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().func().commit()
}

inline fun FragmentManager.inTransactionAllowingStateLoss(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().func().commitAllowingStateLoss()
}

inline fun FragmentManager.inTransactionImmediate(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().func().commitNow()
}

fun FragmentManager.getTopmostFragment(): Fragment? =
    getTopmostFragment(R.id.fragment_container)

fun FragmentManager.getTopmostFragment(@IdRes layout: Int): Fragment? =
    findFragmentById(layout)

fun FragmentManager.isDialogShown(tag: String): Boolean {
    try {
        executePendingTransactions()
    } catch (ignore: Throwable) {

    }
    val dialog = findFragmentByTag(tag)
    return (null != dialog && dialog is DialogFragment)
}


fun FragmentManager.showDialog(dialog: DialogFragment, tag: String) {
    try {
        executePendingTransactions()
    } catch (ignore: Throwable) {

    }
    val prev = findFragmentByTag(tag)
    if (null != prev && prev is DialogFragment) {
        prev.dismissAllowingStateLoss()
    }
    try {
        dialog.show(this, tag)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun FragmentManager.removeDialog(tag: String) {
    try {
        executePendingTransactions()
    } catch (ignore: Throwable) {

    }
    val prev = findFragmentByTag(tag)
    if (null != prev && prev is DialogFragment) {
        prev.dismissAllowingStateLoss()
    }
}
