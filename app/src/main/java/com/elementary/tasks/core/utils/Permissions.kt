package com.elementary.tasks.core.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

object Permissions {

    const val READ_CONTACTS = Manifest.permission.READ_CONTACTS
    const val GET_ACCOUNTS = Manifest.permission.GET_ACCOUNTS
    const val READ_CALENDAR = Manifest.permission.READ_CALENDAR
    const val WRITE_CALENDAR = Manifest.permission.WRITE_CALENDAR
    const val WRITE_EXTERNAL = Manifest.permission.WRITE_EXTERNAL_STORAGE
    const val READ_EXTERNAL = Manifest.permission.READ_EXTERNAL_STORAGE
    const val ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    const val ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
    @SuppressLint("InlinedApi")
    const val BACKGROUND_LOCATION = Manifest.permission.ACCESS_BACKGROUND_LOCATION
    const val READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE
    const val CALL_PHONE = Manifest.permission.CALL_PHONE
    const val RECORD_AUDIO = Manifest.permission.RECORD_AUDIO
    const val BLUETOOTH = Manifest.permission.BLUETOOTH
    const val CAMERA = Manifest.permission.CAMERA
    @RequiresApi(Build.VERSION_CODES.P)
    const val FOREGROUND = Manifest.permission.FOREGROUND_SERVICE

    fun isBgLocationAllowed(context: Context): Boolean {
        if (Module.isQ) {
            return ContextCompat.checkSelfPermission(context, BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    fun checkForeground(context: Context): Boolean {
        if (Module.isPie) {
            if (ContextCompat.checkSelfPermission(context, FOREGROUND) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
            return true
        }
        return true
    }

    fun ensureBackgroundLocation(activity: Activity, requestCode: Int): Boolean {
        return if (isBgLocationAllowed(activity)) {
            true
        } else {
            if (Module.isQ) {
                requestPermission(activity, requestCode, BACKGROUND_LOCATION)
                false
            } else {
                return true
            }
        }
    }

    fun ensureForeground(activity: Activity, requestCode: Int): Boolean {
        return if (checkForeground(activity)) {
            true
        } else {
            if (Module.isPie) {
                requestPermission(activity, requestCode, FOREGROUND)
                false
            } else {
                return true
            }
        }
    }

    fun checkPermission(grantResults: IntArray): Boolean {
        if (grantResults.isEmpty()) {
            return false
        } else {
            for (p in grantResults) {
                if (p != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
            return true
        }
    }

    fun checkPermission(activity: Activity, requestCode: Int, vararg permissions: String): Boolean {
        return if (checkPermission(activity, *permissions)) {
            true
        } else {
            requestPermission(activity, requestCode, *permissions)
            false
        }
    }

    fun checkPermission(a: Context, vararg permissions: String): Boolean {
        if (!Module.isMarshmallow) {
            return true
        }
        var res = true
        for (string in permissions) {
            if (ContextCompat.checkSelfPermission(a, string) != PackageManager.PERMISSION_GRANTED) {
                res = false
            }
        }
        return res
    }

    fun checkPermission(a: Context, permission: String): Boolean {
        return !Module.isMarshmallow || ContextCompat.checkSelfPermission(a, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission(a: Activity, requestCode: Int, vararg permission: String) {
        if (Module.isMarshmallow) {
            val size = permission.size
            if (size == 1) {
                a.requestPermissions(permission, requestCode)
            } else {
                val array = arrayOfNulls<String>(size)
                System.arraycopy(permission, 0, array, 0, size)
                a.requestPermissions(array, requestCode)
            }
        }
    }
}
