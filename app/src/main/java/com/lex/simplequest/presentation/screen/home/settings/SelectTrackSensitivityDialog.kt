package com.lex.simplequest.presentation.screen.home.settings

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import com.lex.simplequest.R
import com.lex.simplequest.databinding.DialogSelectTrackSensitivityBinding
import com.lex.simplequest.presentation.base.BaseDialogFragment

class SelectTrackSensitivityDialog : BaseDialogFragment() {
    companion object {
        private const val ARG_DISTANCE_M = "distance_m"
        private const val ARG_DISTANCE_LIST = "distance_list"

        fun newInstance(distance: Long?, distancesArray: Array<String>): SelectTrackSensitivityDialog =
            SelectTrackSensitivityDialog().apply {
                arguments = Bundle().apply {
                    if (null != distance) {
                        putLong(ARG_DISTANCE_M, distance)
                    }
                    putStringArray(ARG_DISTANCE_LIST, distancesArray)
                }
            }
    }

    private var _viewBinding: DialogSelectTrackSensitivityBinding? = null
    private val viewBinding: DialogSelectTrackSensitivityBinding
    get() = _viewBinding!!

    private var selectedDistance: Long? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogSelectTrackSensitivityBinding.inflate(LayoutInflater.from(context))
        val builder = AlertDialog.Builder(context)
        builder.setView(binding.root)
        builder.setPositiveButton(resources.getString(R.string.settings_apply)) {dialog, which ->
            val clickListener = getTarget(OnDistanceSelectedListener::class.java)
            if (null != selectedDistance) {
                clickListener?.onDistanceSelected(selectedDistance!!)
            }
        }

        val array = arguments!!.getStringArray(ARG_DISTANCE_LIST)!!
        val distance = if (arguments!!.containsKey(ARG_DISTANCE_M)) arguments!!.getLong(ARG_DISTANCE_M) else 5L
        val distanceStr = distance.toString()
        val index = array.indexOf(distanceStr)

        binding.distancesPicker.apply {
            wrapSelectorWheel = true
            displayedValues = array
            minValue = 0
            maxValue = array.lastIndex
            value = if (index > -1) index else 2
            setOnValueChangedListener { picker, oldVal, newVal ->
                selectedDistance = array[newVal].toLong()
            }
        }

        _viewBinding = binding

        return builder.create()
    }

    interface OnDistanceSelectedListener {
        fun onDistanceSelected(distanceM: Long)
    }
}