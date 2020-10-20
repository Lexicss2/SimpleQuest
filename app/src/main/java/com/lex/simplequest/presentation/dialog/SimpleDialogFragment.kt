package com.lex.simplequest.presentation.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AlertDialog
import com.lex.simplequest.R
import com.lex.simplequest.presentation.base.BaseDialogFragment


class SimpleDialogFragment : BaseDialogFragment() {

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_MESSAGE = "message"
        private const val ARG_POSITIVE_BUTTON = "positive_button"
        private const val ARG_NEGATIVE_BUTTON = "negative_button"
        private const val ARG_NEUTRAL_BUTTON = "neutral_button"
        private const val ARG_CANCELLABLE = "cancellable"
        private const val ARG_IGNORE_BACK_KEY = "ignoreBackKey"

        fun newInstance(
            title: String?,
            message: String?,
            positiveButton: String?,
            negativeButton: String?
        ): SimpleDialogFragment =
            SimpleDialogFragment.newInstance(
                title,
                message,
                positiveButton,
                negativeButton,
                null,
                isCancellable = false,
                isIgnoreBackKey = false
            )

        fun newInstance(
            title: String?,
            message: String?,
            positiveButton: String?
        ): SimpleDialogFragment =
            SimpleDialogFragment.newInstance(
                title,
                message,
                positiveButton,
                null,
                null,
                isCancellable = false,
                isIgnoreBackKey = false
            )

        fun newInstance(
            title: String?,
            message: String?,
            positiveButton: String?,
            negativeButton: String?,
            neutralButton: String?,
            isCancellable: Boolean = false,
            isIgnoreBackKey: Boolean = false
        ): SimpleDialogFragment =
            SimpleDialogFragment().apply {
                arguments = Bundle().apply {
                    if (null != title) {
                        putString(ARG_TITLE, title)
                    }
                    if (null != message) {
                        putString(ARG_MESSAGE, message)
                    }
                    if (null != positiveButton) {
                        putString(ARG_POSITIVE_BUTTON, positiveButton)
                    }
                    if (null != negativeButton) {
                        putString(ARG_NEGATIVE_BUTTON, negativeButton)
                    }
                    if (null != neutralButton) {
                        putString(ARG_NEUTRAL_BUTTON, neutralButton)
                    }
                    putBoolean(ARG_CANCELLABLE, isCancellable)
                    putBoolean(ARG_IGNORE_BACK_KEY, isIgnoreBackKey)
                }
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = arguments!!
        val isAllButtonsSet = args.containsKey(ARG_POSITIVE_BUTTON) && args.containsKey(ARG_NEGATIVE_BUTTON) &&
                args.containsKey(ARG_NEUTRAL_BUTTON)
        val builder = AlertDialog.Builder(
            context!!,
            if (isAllButtonsSet) R.style.AppTheme_AlertDialog_AllButtonsSet else R.style.AppTheme_AlertDialog
        )
        val isCancellable = args.getBoolean(ARG_CANCELLABLE)
        if (args.containsKey(ARG_TITLE)) {
            builder.setTitle(args.getString(ARG_TITLE))
        }
        if (args.containsKey(ARG_MESSAGE)) {
            builder.setMessage(args.getString(ARG_MESSAGE))
        }
        if (args.containsKey(ARG_POSITIVE_BUTTON)) {
            builder.setPositiveButton(args.getString(ARG_POSITIVE_BUTTON)) { dialog, which ->
                val clickListener = getTarget(DialogFragmentClickListener::class.java)
                clickListener?.onDialogFragmentClick(this@SimpleDialogFragment, dialog, which)
            }
        }
        if (args.containsKey(ARG_NEGATIVE_BUTTON)) {
            builder.setNegativeButton(args.getString(ARG_NEGATIVE_BUTTON)) { dialog, which ->
                val clickListener = getTarget(DialogFragmentClickListener::class.java)
                clickListener?.onDialogFragmentClick(this@SimpleDialogFragment, dialog, which)
            }
        }
        if (args.containsKey(ARG_NEUTRAL_BUTTON)) {
            builder.setNeutralButton(args.getString(ARG_NEUTRAL_BUTTON)) { dialog, which ->
                val clickListener = getTarget(DialogFragmentClickListener::class.java)
                clickListener?.onDialogFragmentClick(this@SimpleDialogFragment, dialog, which)
            }
        }
        if (args.getBoolean(ARG_IGNORE_BACK_KEY)) {
            builder.setOnKeyListener { _, _, event -> KeyEvent.KEYCODE_BACK == event.keyCode }
        }
        return builder.create().apply {
            setCancelable(isCancellable)
            setCanceledOnTouchOutside(isCancellable)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (isAdded && isResumed) {
            val dismissListener = getTarget(DialogFragmentDismissListener::class.java)
            dismissListener?.onDialogFragmentDismiss(this, dialog)
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        val cancelListener = getTarget(DialogFragmentCancelListener::class.java)
        cancelListener?.onDialogFragmentCancel(this, dialog)
    }
}