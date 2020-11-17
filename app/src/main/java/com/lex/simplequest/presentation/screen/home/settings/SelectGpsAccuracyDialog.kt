package com.lex.simplequest.presentation.screen.home.settings


import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import com.lex.simplequest.Config
import com.lex.simplequest.R
import com.lex.simplequest.databinding.DialogSelectGpsAccuracyBinding
import com.lex.simplequest.presentation.base.BaseDialogFragment
import java.lang.IllegalStateException

class SelectGpsAccuracyDialog : BaseDialogFragment() {
    companion object {
        private const val ARG_TIME_PERIOD_MS = "time_period_ms"
        private const val ARG_TIME_PERIOD_LIST = "time_periods_list"

        fun newInstance(
            timePeriod: Long?,
            timePeriodsArray: Array<String>
        ): SelectGpsAccuracyDialog =
            SelectGpsAccuracyDialog().apply {
                arguments = Bundle().apply {
                    if (null != timePeriod) {
                        putLong(ARG_TIME_PERIOD_MS, timePeriod)
                    }
                    putStringArray(ARG_TIME_PERIOD_LIST, timePeriodsArray)
                }
            }
    }

    private var _viewBinding: DialogSelectGpsAccuracyBinding? = null
    private val viewBinding: DialogSelectGpsAccuracyBinding
        get() = _viewBinding!!

    private var selectedTimePeriodMs: Long? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogSelectGpsAccuracyBinding.inflate(LayoutInflater.from(context))
        val builder = AlertDialog.Builder(context)
        builder.setView(binding.root)
        builder.setPositiveButton(resources.getString(R.string.settings_apply)) { _, _ ->
            val clickListener = getTarget(OnTimePeriodSelectedListener::class.java)
            if (null != selectedTimePeriodMs) {
                clickListener?.onTimePeriodSelected(selectedTimePeriodMs!!)
            }
        }

        val array = arguments!!.getStringArray(ARG_TIME_PERIOD_LIST)!!
        val timePeriod =
            if (arguments!!.containsKey(ARG_TIME_PERIOD_MS)) arguments!!.getLong(ARG_TIME_PERIOD_MS) / 1000L
            else Config.DEFAULT_GPS_ACCURACY_TIME_PERIOD_MS / 1000L
        val timePeriodStr = timePeriod.toString()
        var index = array.indexOf(timePeriodStr)
        if (index == -1) {
            Log.w("qaz", "Selected period $timePeriodStr is not in a list, selecting in the middle")
            index = array.lastIndex / 2
        }

        binding.periodsPicker.apply {
            wrapSelectorWheel = true
            displayedValues = array
            minValue = 0
            maxValue = array.lastIndex
            value = index
            setOnValueChangedListener { _, _, newVal ->
                val period = array[newVal].toLong()
                selectedTimePeriodMs = 1000L * period
            }
        }

        _viewBinding = binding
        return builder.create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

//    fun selectValueIndex(index: Int) {
//        viewBinding.periodsPicker.value = index
//    }

    interface OnTimePeriodSelectedListener {
        fun onTimePeriodSelected(timePeriodMs: Long)
    }
}