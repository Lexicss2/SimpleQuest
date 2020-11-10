package com.lex.simplequest.presentation.screen.home.settings

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import com.lex.simplequest.R
import com.lex.simplequest.presentation.base.BaseDialogFragment

class SelectAccuracyDialog : BaseDialogFragment() {
    companion object {
        private const val ARG_TIME_PERIOD = "time_period"

        fun newInstance(timePeriod: Long?): SelectAccuracyDialog =
            SelectAccuracyDialog().apply {
                arguments = Bundle().apply {
                    if (null != timePeriod) {
                        putLong(ARG_TIME_PERIOD, timePeriod)
                    }
                }
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context, R.style.AppTheme_AlertDialog)
        val timePeriod =
            if (arguments!!.containsKey(ARG_TIME_PERIOD)) arguments!!.getLong(ARG_TIME_PERIOD) else null
        val contentView = LayoutInflater.from(builder.context)
            .inflate(R.layout.dlg_select_accuracy, null)

        builder.setPositiveButton("dfdfdf") { dialog, which ->

        }
        builder.setView(contentView)
        return builder.create().apply {
            setCancelable(true)
            setCanceledOnTouchOutside(true)
        }
    }
}