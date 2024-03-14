package com.elementary.tasks.core.os

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.elementary.tasks.core.utils.Module

@SuppressLint("InlinedApi")
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
  const val CALL_PHONE = Manifest.permission.CALL_PHONE
  const val RECORD_AUDIO = Manifest.permission.RECORD_AUDIO
  const val BLUETOOTH = Manifest.permission.BLUETOOTH
  const val CAMERA = Manifest.permission.CAMERA

  const val FOREGROUND_SERVICE = Manifest.permission.FOREGROUND_SERVICE

  const val POST_NOTIFICATION = Manifest.permission.POST_NOTIFICATIONS

  fun isNotificationsAllowed(context: Context): Boolean {
    if (Module.is13) {
      return ContextCompat.checkSelfPermission(
        context,
        POST_NOTIFICATION
      ) == PackageManager.PERMISSION_GRANTED
    }
    return true
  }

  fun isBgLocationAllowed(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
      context,
      BACKGROUND_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
  }

  fun checkForeground(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
      context,
      FOREGROUND_SERVICE
    ) == PackageManager.PERMISSION_GRANTED
  }

  fun checkPermission(a: Context, vararg permissions: String): Boolean {
    var res = true
    for (string in permissions) {
      if (ContextCompat.checkSelfPermission(a, string) != PackageManager.PERMISSION_GRANTED) {
        res = false
      }
    }
    return res
  }

  fun checkPermission(a: Context, permission: String): Boolean {
    return if (Module.is13 && (permission == READ_EXTERNAL || permission == WRITE_EXTERNAL)) {
      true
    } else {
      ContextCompat.checkSelfPermission(a, permission) == PackageManager.PERMISSION_GRANTED
    }
  }

  @Deprecated("Use PermissionFlow to request permission")
  fun requestPermission(a: Activity, requestCode: Int, vararg permission: String) {
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
