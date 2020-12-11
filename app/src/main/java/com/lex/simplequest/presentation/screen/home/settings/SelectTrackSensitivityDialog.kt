package com.lex.simplequest.presentation.screen.home.settings

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import com.lex.simplequest.Config
import com.lex.simplequest.R
import com.lex.simplequest.databinding.DialogSelectTrackSensitivityBinding
import com.lex.simplequest.presentation.base.BaseDialogFragment

class SelectTrackSensitivityDialog : BaseDialogFragment() {
    companion object {
        private const val ARG_DISTANCE_M = "distance_m"
        private const val ARG_DISTANCE_LIST = "distance_list"

        fun newInstance(
            distance: Long?,
            distancesArray: Array<String>
        ): SelectTrackSensitivityDialog =
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
        val builder = AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Dialog_Alert)
        builder.setView(binding.root)
        builder.setPositiveButton(resources.getString(R.string.settings_apply)) { _, _ ->
            val clickListener = getTarget(OnDistanceSelectedListener::class.java)
            if (null != selectedDistance) {
                clickListener?.onDistanceSelected(selectedDistance!!)
            }
        }

        val array = arguments!!.getStringArray(ARG_DISTANCE_LIST)!!
        val distance =
            if (arguments!!.containsKey(ARG_DISTANCE_M)) arguments!!.getLong(ARG_DISTANCE_M) else Config.DEFAULT_MINIMAL_TRACK_DISTANCE_M
        val distanceStr = distance.toString()
        var index = array.indexOf(distanceStr)
        if (index == -1) {
            Log.w("qaz", "Selected period $index is not in a list, selecting in the middle")
            index = array.lastIndex / 2
        }

        binding.distancesPicker.apply {
            wrapSelectorWheel = true
            displayedValues = array
            minValue = 0
            maxValue = array.lastIndex
            value = index
            setOnValueChangedListener { _, _, newVal ->
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