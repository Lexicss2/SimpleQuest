package com.lex.simplequest.presentation.screen.home.settings

import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import com.lex.simplequest.BuildConfig
import com.lex.simplequest.R
import com.lex.simplequest.databinding.DialogAboutBinding
import com.lex.simplequest.presentation.base.BaseDialogFragment

class AboutDialog : BaseDialogFragment() {
    companion object {

        fun newInstance(): AboutDialog =
            AboutDialog()
    }

    private var _viewBinding: DialogAboutBinding? = null
    private val viewBinding: DialogAboutBinding
        get() = _viewBinding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogAboutBinding.inflate(LayoutInflater.from(context))
        val builder = AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Dialog_Alert)
        builder.setView(binding.root)

        val version = try {
            val info = context!!.packageManager.getPackageInfo(context!!.packageName, 0)
            info.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            "?"
        }

        val buildType = if (BuildConfig.DEBUG) "(debug)" else "(release)"
        binding.descriptionTextView.text = String.format(resources.getString(R.string.settings_about_description), version, buildType)

        _viewBinding = binding

        return builder.create()
    }
}