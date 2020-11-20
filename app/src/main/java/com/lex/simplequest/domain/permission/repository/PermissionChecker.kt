package com.lex.simplequest.domain.permission.repository

interface PermissionChecker {

    fun checkAllPermissionGranted(permissions: Set<Permission>): Boolean
    fun checkAnyPermissionGranted(permissions: Set<Permission>): Boolean

    enum class Permission {
        ACCESS_COARSE_LOCATION,
        ACCESS_FINE_LOCATION,
        CALL_PHONE,
        READ_EXTERNAL_STORAGE,
        WRITE_EXTERNAL_STORAGE
    }
}