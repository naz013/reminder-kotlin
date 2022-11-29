package com.elementary.tasks.core.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import com.elementary.tasks.BuildConfig

object Module {

  const val isPro = BuildConfig.IS_PRO
  val is13 = Build.VERSION.SDK_INT >= 33
  val is12 = Build.VERSION.SDK_INT >= 31
  val is11 = Build.VERSION.SDK_INT >= 30
  val is10 = Build.VERSION.SDK_INT >= 29
  val isPie = Build.VERSION.SDK_INT >= 28
  val isOreoMr1 = Build.VERSION.SDK_INT >= 27
  val isOreo = Build.VERSION.SDK_INT >= 26
  val isMarshmallow = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
  val isNougat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
  val isNougat1 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1

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

  @RequiresApi(Build.VERSION_CODES.M)
  fun hasBiometric(context: Context) =
    BiometricManager.from(context).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS

  fun hasBluetooth(context: Context) =
    context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
}
