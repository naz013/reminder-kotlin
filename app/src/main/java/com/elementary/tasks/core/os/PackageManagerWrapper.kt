package com.elementary.tasks.core.os

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

class PackageManagerWrapper(
  private val context: Context
) {
  private val packageManager = context.packageManager

  fun getInfo(packageName: String): PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
  } else {
    packageManager.getPackageInfo(packageName, 0)
  }

  fun getVersionName(): String = try {
    getInfo(context.packageName).versionName
  } catch (e: Throwable) {
    ""
  }
}