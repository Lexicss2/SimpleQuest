package com.lex.simplequest.presentation.screen.home.settings

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import com.lex.simplequest.Config
import com.lex.simplequest.R
import com.lex.simplequest.databinding.DialogSelectMinimalBatteryLevelBinding
import com.lex.simplequest.presentation.base.BaseDialogFragment

class SelectBatteryLevelDialog : BaseDialogFragment() {
    companion object {
        private const val ARG_BATTERY_LEVEL_PC = "battery_level_pc"
        private const val ARG_AVAILABLE_BATTERY_LEVELS = "available_battery_levels"

        fun newInstance(
            batteryLevel: Int?,
            availableBatteryLevels: Array<String>
        ): SelectBatteryLevelDialog =
            SelectBatteryLevelDialog().apply {
                arguments = Bundle().apply {
                    if (null != batteryLevel) {
                        putInt(ARG_BATTERY_LEVEL_PC, batteryLevel)
                    }
                    putStringArray(ARG_AVAILABLE_BATTERY_LEVELS, availableBatteryLevels)
                }
            }
    }

    private var _viewBinding: DialogSelectMinimalBatteryLevelBinding? = null
    private val viewBinding: DialogSelectMinimalBatteryLevelBinding
        get() = _viewBinding!!

    private var selectedBatteryLevel: Int? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogSelectMinimalBatteryLevelBinding.inflate(LayoutInflater.from(context))
        val builder = AlertDialog.Builder(context)
        builder.setView(binding.root)
        builder.setPositiveButton(resources.getString(R.string.settings_apply)) { _, _ ->
            val clickListener = getTarget(OnBatteryLevelSelectedListener::class.java)
            if (null != selectedBatteryLevel) {
                clickListener?.onBatteryLevelSelected(selectedBatteryLevel!!)
            }
        }

        val array = arguments!!.getStringArray(ARG_AVAILABLE_BATTERY_LEVELS)!!
        val distance =
            if (arguments!!.containsKey(ARG_BATTERY_LEVEL_PC)) arguments!!.getInt(
                ARG_BATTERY_LEVEL_PC
            ) else Config.DEFAULT_BATTERY_LEVEL_PC
        val distanceStr = distance.toString()
        var index = array.indexOf(distanceStr)
        if (index == -1) {
            Log.w("qaz", "Selected level $index is not in a list, selecting in the middle")
            index = array.lastIndex / 2
        }

        binding.batteryLevelsPicker.apply {
            wrapSelectorWheel = true
            displayedValues = array
            minValue = 0
            maxValue = array.lastIndex
            value = index
            setOnValueChangedListener { _, _, newVal ->
                selectedBatteryLevel = array[newVal].toInt()
            }
        }

        _viewBinding = binding

        return builder.create()
    }

    interface OnBatteryLevelSelectedListener {
        fun onBatteryLevelSelected(batteryLevelPc: Int)
    }
}