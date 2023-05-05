package com.elementary.tasks.core.os

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

class PackageManagerWrapper(
  private val context: Context
) {
  val packageManager: PackageManager = context.packageManager

  @Deprecated("After S")
  fun getInstalledApplications(): List<ApplicationInfo> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      packageManager.getInstalledApplications(
        PackageManager.ApplicationInfoFlags.of(0)
      )
    } else {
      packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
    }
  }

  fun getApplicationName(appId: String): String = try {
    getAppInfo(appId).let {
      packageManager.getApplicationLabel(it) as String
    } ?: "???"
  } catch (e: Throwable) {
    "???"
  }

  fun getAppInfo(appId: String): ApplicationInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    packageManager.getApplicationInfo(appId, PackageManager.ApplicationInfoFlags.of(0))
  } else {
    packageManager.getApplicationInfo(appId, 0)
  }

  fun getPackageInfo(packageName: String): PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
  } else {
    packageManager.getPackageInfo(packageName, 0)
  }

  fun getVersionName(): String = try {
    getPackageInfo(context.packageName).versionName
  } catch (e: Throwable) {
    ""
  }
}