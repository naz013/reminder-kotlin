package com.github.naz013.common.system

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.biometric.BiometricManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

class SystemInfo(
  private val context: Context,
  private val buildInfo: BuildInfo,
) {

  val isTablet: Boolean
    get() = Module.isTablet(context)

  val isChromeOs: Boolean
    get() = Module.isChromeOs(context)

  val hasTelephony: Boolean
    get() = Module.hasTelephony(context)

  val hasLocation: Boolean
    get() {
      return context.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION) &&
        googlePlayServicesAvailable
    }

  val hasCamera: Boolean
    get() = Module.hasCamera(context)

  val hasMicrophone: Boolean
    get() = Module.hasMicrophone(context)

  val hasBiometricHardware: Boolean
    get() {
      return BiometricManager.from(context).canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_WEAK
      ) == BiometricManager.BIOMETRIC_SUCCESS
    }

  val currentPackageName: String
    get() = if (buildInfo.isPro) {
      PRO_PACKAGE_NAME
    } else {
      FREE_PACKAGE_NAME
    }

  val googlePlayServicesAvailable: Boolean
    get() {
      val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
      return resultCode == ConnectionResult.SUCCESS
    }

  val isProAppInstalled: Boolean
    get() = isAppInstalled(PRO_PACKAGE_NAME)

  fun isAppInstalled(packageName: String): Boolean {
    val pm = context.packageManager
    return try {
      pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
      true
    } catch (_: PackageManager.NameNotFoundException) {
      false
    }
  }

  companion object {
    const val FREE_PACKAGE_NAME = "com.cray.software.justreminder"
    const val PRO_PACKAGE_NAME = "com.cray.software.justreminderpro"

    val CURRENT_SDK: Int = Build.VERSION.SDK_INT
    const val MIN_SDK: Int = Build.VERSION_CODES.Q
    const val MAX_SDK: Int = Build.VERSION_CODES.BAKLAVA

    val is16: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA
    val is15: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
    val is14: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    val is13: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val is12: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val is11: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
  }
}
