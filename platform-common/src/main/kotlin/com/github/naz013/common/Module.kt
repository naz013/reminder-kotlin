package com.github.naz013.common

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.github.naz013.common.playservices.PlayServices

object Module {

  val CURRENT_SDK: Int = Build.VERSION.SDK_INT
  const val MIN_SDK: Int = Build.VERSION_CODES.O
  const val MAX_SDK: Int = Build.VERSION_CODES.VANILLA_ICE_CREAM

  val is15: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
  val is14: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
  val is13: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
  val is12: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
  val is11: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

  fun isChromeOs(context: Context) =
    context.packageManager.hasSystemFeature("org.chromium.arc.device_management")

  fun hasTelephony(context: Context) =
    context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)

  fun hasLocation(context: Context) =
    context.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION) &&
      PlayServices.isGooglePlayServicesAvailable(context)

  fun hasCamera(context: Context) =
    context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

  fun hasMicrophone(context: Context) =
    context.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)

  fun hasBluetooth(context: Context) =
    context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
}
