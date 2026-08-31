package com.github.naz013.platform

interface SystemInfo {

  val isTablet: Boolean
  val isChromeOs: Boolean
  val hasTelephony: Boolean
  val hasLocation: Boolean
  val hasCamera: Boolean
  val hasMicrophone: Boolean
  val hasBiometricHardware: Boolean
  val hasLedIndication: Boolean
  val currentPackageName: String
  val googlePlayServicesAvailable: Boolean
  val isProAppInstalled: Boolean
  val hasExactAlarmPermission: Boolean

  val is17: Boolean
  val is16: Boolean
  val is15: Boolean
  val is14: Boolean
  val is13: Boolean
  val is12: Boolean
  val is11: Boolean

  val currentSdkLevel: Int
  val minSdkLevel: Int
  val maxSdkLevel: Int

  val applicationName: String

  fun isAppInstalled(packageName: String): Boolean

  companion object {
    const val PRO_PACKAGE_NAME = "com.cray.software.justreminderpro"
    const val FREE_PACKAGE_NAME = "com.cray.software.justreminder"
  }
}
