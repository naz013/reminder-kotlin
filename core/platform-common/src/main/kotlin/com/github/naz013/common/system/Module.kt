package com.github.naz013.common.system

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build

object Module {

  val CURRENT_SDK: Int = Build.VERSION.SDK_INT
  const val MIN_SDK: Int = Build.VERSION_CODES.Q
  const val MAX_SDK: Int = Build.VERSION_CODES.CINNAMON_BUN

  val is17: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN
  val is16: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA
  val is15: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
  val is14: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
  val is13: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
  val is12: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
  val is11: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

  fun isTablet(context: Context): Boolean {
    val screenLayout = context.resources.configuration.screenLayout
    val screenSize = screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
    return screenSize >= Configuration.SCREENLAYOUT_SIZE_LARGE
  }

  fun isChromeOs(context: Context) =
    context.packageManager.hasSystemFeature("org.chromium.arc.device_management")

  fun hasTelephony(context: Context) =
    context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)

  fun hasCamera(context: Context) =
    context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

  fun hasMicrophone(context: Context) =
    context.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
}
