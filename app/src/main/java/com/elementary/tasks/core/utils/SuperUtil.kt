package com.elementary.tasks.core.utils

import android.app.Activity
import android.app.ActivityManager
import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.media.AudioManager
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.util.Base64
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.elementary.tasks.R
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.services.GeolocationService
import com.elementary.tasks.core.utils.ui.Dialogues
import com.elementary.tasks.reminder.create.fragments.ReminderInterface
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import timber.log.Timber
import java.io.UnsupportedEncodingException
import java.util.UUID

object SuperUtil {

  fun isPhoneCallActive(context: Context): Boolean {
    val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    return manager.mode == AudioManager.MODE_IN_CALL ||
      manager.mode == AudioManager.MODE_IN_COMMUNICATION
  }

  fun normalizeSummary(summary: String): String {
    return if (summary.length > Configs.MAX_REMINDER_SUMMARY_LENGTH) {
      summary.substring(0, Configs.MAX_REMINDER_SUMMARY_LENGTH)
    } else {
      summary
    }
  }

  fun wakeDevice(
    activity: Activity,
    id: String = UUID.randomUUID().toString()
  ): PowerManager.WakeLock {
    val screenLock = (activity.getSystemService(Context.POWER_SERVICE) as PowerManager)
      .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "reminder:ReminderAPPTAG:$id")
    screenLock.acquire(10 * 60 * 1000L /*10 minutes*/)
    return screenLock
  }

  @Suppress("DEPRECATION")
  fun unlockOff(activity: Activity, window: Window) {
    Timber.d("unlockOff: ")
    if (Module.isOreoMr1) {
      activity.setShowWhenLocked(false)
    } else {
      window.clearFlags(
        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
          or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
      )
    }
  }

  @Suppress("DEPRECATION")
  fun unlockOn(activity: Activity, window: Window) {
    Timber.d("unlockOn: ")
    val keyguardManager = activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager?
    keyguardManager?.requestDismissKeyguard(activity, null)
  }

  @Suppress("DEPRECATION")
  fun turnScreenOff(activity: Activity, window: Window, wakeLock: PowerManager.WakeLock? = null) {
    Timber.d("turnScreenOff: ")
    if (wakeLock?.isHeld == true) {
      wakeLock.release()
    }
    if (Module.isOreoMr1) {
      activity.setShowWhenLocked(false)
      activity.setTurnScreenOn(false)
      window.clearFlags(
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
          or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
      )
    } else {
      window.clearFlags(
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
          or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
          or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
      )
    }
    unlockOff(activity, window)
  }

  @Suppress("DEPRECATION")
  fun turnScreenOn(activity: Activity, window: Window) {
    Timber.d("turnScreenOn: ")
    if (Module.isOreoMr1) {
      activity.setTurnScreenOn(true)
      activity.setShowWhenLocked(true)
      window.addFlags(
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
          or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
      )
    } else {
      window.addFlags(
        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
          or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
          or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
      )
    }
    unlockOn(activity, window)
  }

  fun hasVolumePermission(context: Context?): Boolean {
    if (context == null) return false
    val notificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
    return notificationManager != null && notificationManager.isNotificationPolicyAccessGranted
  }

  fun stopService(context: Context, clazz: Class<*>) {
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

  fun isHeadsetUsing(context: Context): Boolean {
    val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
    return manager != null && (manager.isBluetoothA2dpOn || manager.isWiredHeadsetOn)
  }

  fun getString(fragment: Fragment, id: Int): String {
    return if (fragment.isAdded) {
      fragment.getString(id)
    } else {
      ""
    }
  }

  fun isDoNotDisturbEnabled(context: Context): Boolean {
    val mNotificationManager = context.getSystemService(
      Context.NOTIFICATION_SERVICE
    ) as NotificationManager

    val filter = mNotificationManager.currentInterruptionFilter

    return if (
      filter == NotificationManager.INTERRUPTION_FILTER_ALARMS ||
      filter == NotificationManager.INTERRUPTION_FILTER_NONE
    ) {
      Timber.d("isDoNotDisturbEnabled: true")
      true
    } else {
      Timber.d("isDoNotDisturbEnabled: false")
      false
    }
  }

  fun checkNotificationPermission(activity: Context): Boolean {
    val notificationManager =
      activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return notificationManager.isNotificationPolicyAccessGranted
  }

  fun askNotificationPermission(activity: Activity, dialogues: Dialogues) {
    val builder = dialogues.getMaterialDialog(activity)
    builder.setMessage(R.string.for_correct_work_of_application)
    builder.setPositiveButton(R.string.grant) { dialog, _ ->
      dialog.dismiss()
      val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
      try {
        activity.startActivity(intent)
      } catch (ignored: ActivityNotFoundException) {
      }
    }
    builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
    builder.create().show()
  }

  fun checkLocationEnable(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val isGPSEnabled = locationManager
      .isProviderEnabled(LocationManager.GPS_PROVIDER)
    val isNetworkEnabled = locationManager
      .isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    return !(!isGPSEnabled && !isNetworkEnabled)
  }

  fun showLocationAlert(context: Context, callbacks: ReminderInterface) {
    callbacks.showSnackbar(
      context.getString(R.string.gps_not_enabled),
      context.getString(R.string.action_settings)
    ) {
      val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
      context.startActivity(intent)
    }
  }

  fun isGooglePlayServicesAvailable(a: Context): Boolean {
    val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(a)
    return resultCode == ConnectionResult.SUCCESS
  }

  fun checkGooglePlayServicesAvailability(a: Activity): Boolean {
    val googleAPI = GoogleApiAvailability.getInstance()
    val result = googleAPI.isGooglePlayServicesAvailable(a)
    Timber.d("checkGooglePlayServicesAvailability: $result")
    return if (result != ConnectionResult.SUCCESS) {
      if (googleAPI.isUserResolvableError(result)) {
        googleAPI.getErrorDialog(a, result, 69)?.show()
      }
      false
    } else {
      true
    }
  }

  private fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
      if (serviceClass.name == service.service.className) {
        return true
      }
    }
    return false
  }

  fun getAfterTime(timeString: String): Long {
    return if (timeString.length == 6 && !timeString.matches("000000".toRegex())) {
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
  }

  fun isAppInstalled(context: Context, packageName: String): Boolean {
    val pm = context.packageManager
    val installed: Boolean = try {
      pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
      true
    } catch (e: PackageManager.NameNotFoundException) {
      false
    }
    return installed
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

  fun launchMarket(context: Context) {
    val uri = Uri.parse("market://details?id=" + context.packageName)
    val goToMarket = Intent(Intent.ACTION_VIEW, uri)
    try {
      context.startActivity(goToMarket)
    } catch (e: ActivityNotFoundException) {
      Toast.makeText(
        context,
        context.getString(R.string.could_not_launch_market),
        Toast.LENGTH_SHORT
      ).show()
    }
  }
}
