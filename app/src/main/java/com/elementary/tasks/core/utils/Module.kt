package com.elementary.tasks.core.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.elementary.tasks.BuildConfig

object Module {

  val currentSdk: Int = Build.VERSION.SDK_INT
  const val minSdk: Int = Build.VERSION_CODES.O
  const val maxSdk: Int = Build.VERSION_CODES.UPSIDE_DOWN_CAKE

  const val isPro: Boolean = BuildConfig.IS_PRO
  val isDebug: Boolean = BuildConfig.DEBUG
  val is13: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
  val is12: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
  val is11: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

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
