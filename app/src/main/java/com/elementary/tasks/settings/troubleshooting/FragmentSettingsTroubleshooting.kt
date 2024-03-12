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
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.analytics.Screen
import com.elementary.tasks.core.analytics.ScreenUsedEvent
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.ui.visibleGone
import com.elementary.tasks.databinding.FragmentSettingsTroubleshootingBinding
import com.elementary.tasks.navigation.fragments.BaseSettingsFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class FragmentSettingsTroubleshooting :
  BaseSettingsFragment<FragmentSettingsTroubleshootingBinding>() {

  private val viewModel by viewModel<TroubleshootingViewModel>()

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsTroubleshootingBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.disableOptimizationButton.setOnClickListener { openBatteryOptimizationSettings() }
    binding.sendLogsPrefs.setOnClickListener { viewModel.sendLogs() }

    lifecycle.addObserver(viewModel)
    viewModel.hideBatteryOptimizationCard.nonNullObserve(viewLifecycleOwner) {
      binding.batterSaverOption.visibleGone(!it)
    }
    viewModel.showEmptyView.nonNullObserve(viewLifecycleOwner) {
      binding.emptyStateView.visibleGone(it)
    }
    viewModel.showSendLogs.nonNullObserve(viewLifecycleOwner) {
      binding.sendLogsPrefs.visibleGone(it)
    }
    viewModel.sendLogFile.nonNullObserve(viewLifecycleOwner) { sendLogs(it) }
  }

  private fun sendLogs(file: File) {
    TelephonyUtil.sendMail(
      context = requireContext(),
      email = "feedback.cray@gmail.com",
      subject = "Issue Logs",
      message = "Hi,\n\nHere is logs for my issue.\n\nIssue description: \n\nBest regards\n",
      file = file
    )
  }

  private fun openBatteryOptimizationSettings() {
    analyticsEventSender.send(ScreenUsedEvent(Screen.TROUBLESHOOTING))

    when (Build.MANUFACTURER) {
      "samsung" -> {
        val intent = Intent()
        intent.component = ComponentName(
          /* pkg = */ "com.samsung.android.lool",
          /* cls = */ "com.samsung.android.sm.ui.battery.BatteryActivity"
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
          /* pkg = */ "com.miui.securitycenter",
          /* cls = */ "com.miui.permcenter.autostart.AutoStartManagementActivity"
        )
        try {
          startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
          try {
            intent = Intent()
            intent.setComponent(
              ComponentName(
                /* pkg = */ "com.miui.powerkeeper",
                /* cls = */ "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"
              )
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
          /* pkg = */ "com.huawei.systemmanager",
          /* cls = */ "com.huawei.systemmanager.optimize.process.ProtectActivity"
        )
        try {
          startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
          openAppSettings()
        }
      }

      else -> {
        openAppSettings()
      }
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
