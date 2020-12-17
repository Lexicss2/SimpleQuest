package com.lex.simplequest.presentation.screen.home.settings

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import com.lex.simplequest.Config
import com.lex.simplequest.R
import com.lex.simplequest.databinding.DialogSelectMinimalDispalcementBinding
import com.lex.simplequest.presentation.base.BaseDialogFragment

class SelectMinimalDisplacementDialog : BaseDialogFragment() {
    companion object {
        private const val ARG_MINIMAL_DISPLACEMENT_M = "minimal_displacement_m"
        private const val ARG_MINIMAL_DISPLACEMENTS_LIST = "minimal_displacements_list"

        fun newInstance(
            minimalDisplacement: Long?,
            minimalDisplacementsArray: Array<String>
        ): SelectMinimalDisplacementDialog =
            SelectMinimalDisplacementDialog().apply {
                arguments = Bundle().apply {
                    if (null != minimalDisplacement) {
                        putLong(ARG_MINIMAL_DISPLACEMENT_M, minimalDisplacement)
                    }
                    putStringArray(ARG_MINIMAL_DISPLACEMENTS_LIST, minimalDisplacementsArray)
                }
            }
    }

    private var _viewBinding: DialogSelectMinimalDispalcementBinding? = null
    private val viewBinding: DialogSelectMinimalDispalcementBinding
        get() = _viewBinding!!

    private var selectedMinimalDisplacememntM: Long? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogSelectMinimalDispalcementBinding.inflate(LayoutInflater.from(context))
        val builder = AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Dialog_Alert)
        builder.setView(binding.root)
        builder.setPositiveButton(resources.getString(R.string.settings_apply)) { _, _ ->
            val clickListener = getTarget(OnMinimalDisplacementSelectedListener::class.java)
            if (null != selectedMinimalDisplacememntM) {
                clickListener?.onMinimalDisplacementSelected(selectedMinimalDisplacememntM!!)
            }
        }

        val array = arguments!!.getStringArray(ARG_MINIMAL_DISPLACEMENTS_LIST)!!
        val displacement =
            if (arguments!!.containsKey(ARG_MINIMAL_DISPLACEMENT_M)) arguments!!.getLong(
                ARG_MINIMAL_DISPLACEMENT_M
            )
            else Config.DEFAULT_GPS_ACCURACY_TIME_PERIOD_MS / 1000L
        val displacementStr = displacement.toString()
        var index = array.indexOf(displacementStr)

        if (index == -1) {
            index = array.lastIndex / 2
        }

        binding.displacementsPicker.apply {
            wrapSelectorWheel = true
            displayedValues = array
            minValue = 0
            maxValue = array.lastIndex
            value = index
            setOnValueChangedListener { _, _, newVal ->
                selectedMinimalDisplacememntM = array[newVal].toLong()
            }
        }

        _viewBinding = binding
        return builder.create()
    }

    interface OnMinimalDisplacementSelectedListener {
        fun onMinimalDisplacementSelected(minimalDisplacement: Long)
    }
}