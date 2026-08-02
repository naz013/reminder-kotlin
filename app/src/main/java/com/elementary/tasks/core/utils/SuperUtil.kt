package com.elementary.tasks.core.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Base64
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.elementary.tasks.core.services.GeolocationService
import com.github.naz013.common.Permissions
import java.io.UnsupportedEncodingException

object SuperUtil {

  fun isPhoneCallActive(context: Context): Boolean {
    val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    return manager.mode == AudioManager.MODE_IN_CALL ||
      manager.mode == AudioManager.MODE_IN_COMMUNICATION
  }

  fun normalizeSummary(summary: String): String =
    if (summary.length > Configs.MAX_REMINDER_SUMMARY_LENGTH) {
      summary.substring(0, Configs.MAX_REMINDER_SUMMARY_LENGTH)
    } else {
      summary
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

  fun getAfterTime(timeString: String): Long =
    if (timeString.length == 6 && !timeString.matches("000000".toRegex())) {
      val hours = timeString.substring(0, 2)
      val minutes = timeString.substring(2, 4)
      val seconds = timeString.substring(4, 6)
      val hour = Integer.parseInt(hours)
      val minute = Integer.parseInt(minutes)
      val sec = Integer.parseInt(seconds)
      val s: Long = 1000
      val m = s * 60
      val h = m * 60
      hour * h + minute * m + sec * s
    } else {
      0
    }

  fun decrypt(string: String): String {
    var result = ""
    val bytes = Base64.decode(string, Base64.DEFAULT)
    try {
      result = String(bytes, charset("UTF-8"))
    } catch (e1: UnsupportedEncodingException) {
      e1.printStackTrace()
    }
    return result
  }

  fun encrypt(string: String): String {
    if (string.isEmpty()) return ""
    var input: ByteArray? = null
    try {
      input = string.toByteArray(charset("UTF-8"))
    } catch (e: UnsupportedEncodingException) {
      e.printStackTrace()
    }
    return Base64.encodeToString(input, Base64.DEFAULT).trim()
  }
}
