package com.lex.simplequest.presentation.base

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

import com.lex.simplequest.presentation.utils.isInternetConnectivityError
import com.lex.simplequest.presentation.utils.showDialog
import java.net.MalformedURLException

object DefaultErrorHandler {

    const val DLG_ERROR = "dlgError"

    fun showError(
        activity: FragmentActivity,
        message: String?,
        error: Throwable,
        vararg args: Any
    ) {
        showError(
            activity.supportFragmentManager,
            activity,
            message,
            error,
            DLG_ERROR,
            args
        )
    }

    fun showError(
        fragment: Fragment,
        message: String?,
        error: Throwable,
        vararg args: Any
    ) {
        showError(
            fragment.childFragmentManager,
            fragment.context!!,
            message,
            error,
            DLG_ERROR,
            args
        )
    }

    fun showError(
        fragment: Fragment,
        message: String?,
        error: Throwable,
        tag: String,
        vararg args: Any
    ) {
        showError(
            fragment.childFragmentManager,
            fragment.context!!,
            message,
            error,
            tag,
            args
        )
    }

    @Suppress("UNUSED_PARAMETER")
    private fun showError(
        fragmentManager: FragmentManager,
        context: Context,
        message: String?,
        error: Throwable,
        tag: String,
        vararg args: Any
    ) {
//        val dialog = when {
//            error.isInternetConnectivityError() -> {
//                SimpleDialogFragment.newInstance(
//                    context.getString(R.string.error_title),
//                    message ?: context.getString(R.string.error_network_no_internet),
//                    context.getString(R.string.ok)
//                )
//            }
//            error is MalformedURLException ->
//                SimpleDialogFragment.newInstance(
//                    context.getString(R.string.error_title),
//                    message ?: context.getString(R.string.error_network_internal),
//                    context.getString(R.string.ok)
//                )
//            error is IpicApiException -> {
//                SimpleDialogFragment.newInstance(
//                    context.getString(R.string.error_title),
//                    message ?: error.message
//                    ?: context.getString(R.string.error_network_server_error),
//                    context.getString(R.string.ok)
//                )
//            }
//            error is HttpException ->
//                SimpleDialogFragment.newInstance(
//                    context.getString(R.string.error_title),
//                    message ?: context.getString(R.string.error_network_server_error),
//                    context.getString(R.string.ok)
//                )
//            error is AccessPointsUnavailableException ->
//                SimpleDialogFragment.newInstance(
//                    context.getString(R.string.error_access_points_unavailable_title),
//                    context.getString(R.string.error_access_points_unavailable_message),
//                    context.getString(R.string.ok)
//                )
//            error is PaymentException -> {
//                SimpleDialogFragment.newInstance(
//                    context.getString(R.string.error_title),
//                    context.getString(R.string.error_payment_failed),
//                    context.getString(R.string.ok)
//                )
//            }
//            error is StripeException -> {
//                SimpleDialogFragment.newInstance(
//                    context.getString(R.string.error_title),
//                    error.message ?: context.getString(R.string.error_msg_general),
//                    context.getString(R.string.ok)
//                )
//            }
//            else -> {
//                SimpleDialogFragment.newInstance(
//                    context.getString(R.string.error_title),
//                    message ?: context.getString(R.string.error_msg_general),
//                    context.getString(R.string.ok)
//                )
//            }
//        }
//        fragmentManager.showDialog(dialog, tag)
    }

    fun getErrorString(context: Context, error: Throwable): String =
        ""
//        when (error) {
//            is ValidationException -> error.message ?: context.getString(R.string.error_msg_general)
//            else -> context.getString(R.string.error_msg_general)
//        }
}