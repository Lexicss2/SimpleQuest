package com.lex.simplequest.presentation.screen.home.settings


import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import com.lex.simplequest.R
import com.lex.simplequest.databinding.DlgSelectAccuracyBinding
import com.lex.simplequest.presentation.base.BaseDialogFragment

class SelectAccuracyDialog : BaseDialogFragment() {
    companion object {
        private const val ARG_TIME_PERIOD_MS = "time_period_ms"
        private const val ARG_TIME_PERIOD_LIST = "time_periods_list"

        fun newInstance(timePeriod: Long?, timePeriodsArray: Array<String>): SelectAccuracyDialog =
            SelectAccuracyDialog().apply {
                arguments = Bundle().apply {
                    if (null != timePeriod) {
                        putLong(ARG_TIME_PERIOD_MS, timePeriod)
                    }
                    putStringArray(ARG_TIME_PERIOD_LIST, timePeriodsArray)
                }
            }
    }

    private var _viewBinding: DlgSelectAccuracyBinding? = null
    private val viewBinding: DlgSelectAccuracyBinding
    get() = _viewBinding!!

    private var timePeriodSelectedListener: OnTimePeriodSelectedListener? = null
    private var selectedTimePeriodMs: Long? = null

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val builder = AlertDialog.Builder(context, R.style.AppTheme_AlertDialog)
//        val timePeriod =
//            if (arguments!!.containsKey(ARG_TIME_PERIOD)) arguments!!.getLong(ARG_TIME_PERIOD) else null
//        val contentView = LayoutInflater.from(builder.context)
//            .inflate(R.layout.dlg_select_accuracy, null)
//        //val numberPicker = contentView.findViewById<NumberPicker>()
//
//        builder.setPositiveButton(resources.getString(R.string.settings_apply)) { dialog, which ->
//
//        }
//        builder.setView(contentView)
//        return builder.create().apply {
//            setCancelable(true)
//            setCanceledOnTouchOutside(true)
//        }
//    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DlgSelectAccuracyBinding.inflate(LayoutInflater.from(context))
        val builder = AlertDialog.Builder(context)
        builder.setView(binding.root)
        builder.setPositiveButton(resources.getString(R.string.settings_apply)) { dialog, which ->
            val clickListener = getTarget(OnTimePeriodSelectedListener::class.java)
            if (null != selectedTimePeriodMs) {
                clickListener?.onTimePeriodSelected(selectedTimePeriodMs!!)
            }
        }

//        binding.apply {
//            val values = arrayOf("1", "2", "5", "10", "30", "60", "120")
//            periodsPicker.displayedValues = values
//            periodsPicker.wrapSelectorWheel = true
//            periodsPicker.minValue = 0
//            periodsPicker.maxValue = 6
//            //periodsPicker.value = 5
//        }

        val array = arguments!!.getStringArray(ARG_TIME_PERIOD_LIST)!!
        val timePeriod = if (arguments!!.containsKey(ARG_TIME_PERIOD_MS)) arguments!!.getLong(ARG_TIME_PERIOD_MS) / 1000L else 5L
        val timePeriodStr = timePeriod.toString()
        val index = array.indexOf(timePeriodStr)

        binding.periodsPicker.apply {
            wrapSelectorWheel = true
            displayedValues = array
            minValue = 0
            maxValue = array.lastIndex
            value = if (index > -1) index else 2
            setOnValueChangedListener { picker, oldVal, newVal ->
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

    fun selectValueIndex(index: Int) {
        viewBinding.periodsPicker.value = index
    }

    interface OnTimePeriodSelectedListener {
        fun onTimePeriodSelected(timePeriodMs: Long)
    }
}