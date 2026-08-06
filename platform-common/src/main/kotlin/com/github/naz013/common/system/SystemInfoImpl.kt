package com.github.naz013.common.system

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import androidx.biometric.BiometricManager
import com.github.naz013.common.system.SystemInfo.Companion.FREE_PACKAGE_NAME
import com.github.naz013.common.system.SystemInfo.Companion.PRO_PACKAGE_NAME
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

internal class SystemInfoImpl(
  private val context: Context,
  private val buildInfo: BuildInfo,
) : SystemInfo {

  override val isTablet: Boolean
    get() = Module.isTablet(context)

  override val isChromeOs: Boolean
    get() = Module.isChromeOs(context)

  override val hasTelephony: Boolean
    get() = Module.hasTelephony(context)

  override val hasLocation: Boolean
    get() {
      return context.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION) &&
        googlePlayServicesAvailable
    }

  override val hasCamera: Boolean
    get() = Module.hasCamera(context)

  override val hasMicrophone: Boolean
    get() = Module.hasMicrophone(context)

  override val hasBiometricHardware: Boolean
    get() {
      return BiometricManager.from(context).canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_WEAK
      ) == BiometricManager.BIOMETRIC_SUCCESS
    }

  override val hasLedIndication: Boolean
    get() = try {
      val resources = Resources.getSystem()
      val resId = resources.getIdentifier("config_defaultNotificationLedOn", "bool", "android")
      resId != 0 && resources.getBoolean(resId)
    } catch (_: Exception) {
      false
    }

  override val currentPackageName: String
    get() = if (buildInfo.isPro) {
      PRO_PACKAGE_NAME
    } else {
      FREE_PACKAGE_NAME
    }

  override val googlePlayServicesAvailable: Boolean
    get() {
      val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
      return resultCode == ConnectionResult.SUCCESS
    }

  override val isProAppInstalled: Boolean
    get() = isAppInstalled(PRO_PACKAGE_NAME)

  override val currentSdkLevel: Int = Build.VERSION.SDK_INT
  override val minSdkLevel: Int = Build.VERSION_CODES.Q
  override val maxSdkLevel: Int = Build.VERSION_CODES.CINNAMON_BUN

  override val is17: Boolean = currentSdkLevel >= Build.VERSION_CODES.CINNAMON_BUN
  override val is16: Boolean = currentSdkLevel >= Build.VERSION_CODES.BAKLAVA
  override val is15: Boolean = currentSdkLevel >= Build.VERSION_CODES.VANILLA_ICE_CREAM
  override val is14: Boolean = currentSdkLevel >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
  override val is13: Boolean = currentSdkLevel >= Build.VERSION_CODES.TIRAMISU
  override val is12: Boolean = currentSdkLevel >= Build.VERSION_CODES.S
  override val is11: Boolean = currentSdkLevel >= Build.VERSION_CODES.R

  override fun isAppInstalled(packageName: String): Boolean {
    val pm = context.packageManager
    return try {
      pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
      true
    } catch (_: PackageManager.NameNotFoundException) {
      false
    }
  }
}
