package com.elementary.tasks.core.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.elementary.tasks.core.services.GeolocationService
import com.github.naz013.common.Permissions

object SuperUtil {

  fun isPhoneCallActive(context: Context): Boolean {
    val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    return manager.mode == AudioManager.MODE_IN_CALL ||
      manager.mode == AudioManager.MODE_IN_COMMUNICATION
  }

  fun stopService(
    context: Context,
    clazz: Class<*>,
  ) {
    context.stopService(Intent(context, clazz))
  }

  fun startGpsTracking(context: Context) {
    if (
      !Permissions.checkForeground(context) ||
      isServiceRunning(context, GeolocationService::class.java) ||
      !Permissions.isBgLocationAllowed(context)
    ) {
      return
    }
    val intent = Intent(context, GeolocationService::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    ContextCompat.startForegroundService(context, intent)
  }

  fun getString(
    fragment: Fragment,
    id: Int,
  ): String =
    if (fragment.isAdded) {
      fragment.getString(id)
    } else {
      ""
    }

  private fun isServiceRunning(
    context: Context,
    serviceClass: Class<*>,
  ): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
      if (serviceClass.name == service.service.className) {
        return true
      }
    }
    return false
  }
}
