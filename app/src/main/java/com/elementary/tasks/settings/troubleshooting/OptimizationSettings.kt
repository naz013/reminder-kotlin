package com.elementary.tasks.settings.troubleshooting

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.CATEGORY_DEFAULT
import android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_NO_HISTORY
import android.net.Uri
import android.os.Build
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.elementary.tasks.R

interface OptimizationSettingsLauncher {
  fun launch()
}

private class OptimizationSettingsLauncherImpl(
  private val context: Context,
) : OptimizationSettingsLauncher {
  override fun launch() {
    when (Build.MANUFACTURER) {
      "samsung" -> {
        val intent = Intent()
        intent.component =
          ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")
        try {
          context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
          openAppSettings()
        }
      }

      "xiaomi" -> {
        var intent = Intent()
        intent.component =
          ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
        try {
          context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
          try {
            intent = Intent()
            intent.setComponent(
              ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"),
            )
            intent.putExtra("package_name", context.packageName)
            intent.putExtra("package_label", context.getText(R.string.app_name))
            context.startActivity(intent)
          } catch (_: ActivityNotFoundException) {
            openAppSettings()
          }
        }
      }

      "huawei" -> {
        val intent = Intent()
        intent.component =
          ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")
        try {
          context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
          openAppSettings()
        }
      }

      else -> openAppSettings()
    }
  }

  private fun openAppSettings() {
    val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
    with(intent) {
      data = Uri.fromParts("package", context.packageName, null)
      addCategory(CATEGORY_DEFAULT)
      addFlags(FLAG_ACTIVITY_NEW_TASK)
      addFlags(FLAG_ACTIVITY_NO_HISTORY)
      addFlags(FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    }
    context.startActivity(intent)
  }
}

@Composable
fun rememberOptimizationSettingsLauncher(): OptimizationSettingsLauncher {
  val context = LocalContext.current
  return OptimizationSettingsLauncherImpl(context)
}
