package com.lex.simplequest.presentation.dialog

import android.content.DialogInterface
import com.lex.simplequest.presentation.base.BaseDialogFragment


interface DialogFragmentDismissListener {
    fun onDialogFragmentDismiss(dialogFragment: BaseDialogFragment, dialog: DialogInterface)
}
