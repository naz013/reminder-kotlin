package com.elementary.tasks.settings.troubleshooting

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.Intent.CATEGORY_DEFAULT
import android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_NO_HISTORY
import android.net.Uri
import android.os.Build
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment
import com.elementary.tasks.notes.ObserveNonNull
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import org.koin.androidx.viewmodel.ext.android.viewModel

class TroubleshootingFragment : BaseComposeToolbarFragment() {

  private val viewModel by viewModel<TroubleshootingViewModel>()

  @Composable
  override fun Content() {
    LaunchedEffect(viewModel) { lifecycle.addObserver(viewModel) }
    val hideBatteryOptimizationCard by viewModel.hideBatteryOptimizationCard.observeAsState(false)
    val showEmptyView by viewModel.showEmptyView.observeAsState(false)
    val showSendLogs by viewModel.showSendLogs.observeAsState(false)
    viewModel.sendLogFile.ObserveNonNull { sendLogs(it) }

    TroubleshootingScreen(
      showSendLogs = showSendLogs,
      showBatteryOptimizationCard = !hideBatteryOptimizationCard,
      showEmptyView = showEmptyView,
      onSendLogsClick = viewModel::sendLogs,
      onDisableOptimizationClick = ::openBatteryOptimizationSettings,
    )
  }

  private fun sendLogs(file: java.io.File) {
    TelephonyUtil.sendMail(
      context = requireContext(),
      email = "feedback.cray@gmail.com",
      subject = "Issue Logs",
      message = "Hi,\n\nHere is logs for my issue.\n\nIssue description: \n\nBest regards\n",
      file = file,
    )
  }

  private fun openBatteryOptimizationSettings() {
    analyticsEventSender.send(ScreenUsedEvent(Screen.TROUBLESHOOTING))

    when (Build.MANUFACTURER) {
      "samsung" -> {
        val intent = Intent()
        intent.component = ComponentName(
          "com.samsung.android.lool",
          "com.samsung.android.sm.ui.battery.BatteryActivity",
        )
        try {
          startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
          openAppSettings()
        }
      }

      "xiaomi" -> {
        var intent = Intent()
        intent.component = ComponentName(
          "com.miui.securitycenter",
          "com.miui.permcenter.autostart.AutoStartManagementActivity",
        )
        try {
          startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
          try {
            intent = Intent()
            intent.setComponent(
              ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"),
            )
            intent.putExtra("package_name", viewModel.packageName())
            intent.putExtra("package_label", getText(R.string.app_name))
            startActivity(intent)
          } catch (anfe: ActivityNotFoundException) {
            openAppSettings()
          }
        }
      }

      "huawei" -> {
        val intent = Intent()
        intent.component = ComponentName(
          "com.huawei.systemmanager",
          "com.huawei.systemmanager.optimize.process.ProtectActivity",
        )
        try {
          startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
          openAppSettings()
        }
      }

      else -> openAppSettings()
    }
  }

  private fun openAppSettings() {
    val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
    with(intent) {
      data = Uri.fromParts("package", viewModel.packageName(), null)
      addCategory(CATEGORY_DEFAULT)
      addFlags(FLAG_ACTIVITY_NEW_TASK)
      addFlags(FLAG_ACTIVITY_NO_HISTORY)
      addFlags(FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    }
    startActivity(intent)
  }

  override fun getTitle(): String = getString(R.string.troubleshooting)
}
