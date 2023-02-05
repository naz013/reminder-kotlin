package com.elementary.tasks.core.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.elementary.tasks.BuildConfig

object Module {

  const val isPro = BuildConfig.IS_PRO
  val is13 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
  val is12 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
  val is11 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
  val is10 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
  val isPie = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
  val isOreoMr1 = Build.VERSION.SDK_INT >= 27

  fun isChromeOs(context: Context) =
    context.packageManager.hasSystemFeature("org.chromium.arc.device_management")

  fun hasTelephony(context: Context) =
    context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)

  fun hasLocation(context: Context) =
    context.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION) &&
      SuperUtil.isGooglePlayServicesAvailable(context)

  fun hasCamera(context: Context) =
    context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

  fun hasMicrophone(context: Context) =
    context.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)

  fun hasBluetooth(context: Context) =
    context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
}
