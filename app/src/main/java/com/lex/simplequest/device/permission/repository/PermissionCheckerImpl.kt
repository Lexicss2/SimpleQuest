package com.lex.simplequest.device.permission.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.lex.simplequest.domain.permission.repository.PermissionChecker

class PermissionCheckerImpl(ctx: Context) : PermissionChecker {
    private val context = ctx

    override fun checkAllPermissionGranted(permissions: Set<PermissionChecker.Permission>): Boolean =
        if (permissions.isNotEmpty()) {
            permissions.all { permission ->
                PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                    context,
                    permission.asAndroidPermission()
                )
            }
        } else true


    override fun checkAnyPermissionGranted(permissions: Set<PermissionChecker.Permission>): Boolean =
        if (permissions.isNotEmpty()) {
            permissions.any { permission ->
                PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                    context,
                    permission.asAndroidPermission()
                )
            }
        } else true

    private fun PermissionChecker.Permission.asAndroidPermission(): String =
        when (this) {
            PermissionChecker.Permission.ACCESS_COARSE_LOCATION -> Manifest.permission.ACCESS_COARSE_LOCATION
            PermissionChecker.Permission.ACCESS_FINE_LOCATION -> Manifest.permission.ACCESS_FINE_LOCATION
            PermissionChecker.Permission.CALL_PHONE -> Manifest.permission.CALL_PHONE
        }
}