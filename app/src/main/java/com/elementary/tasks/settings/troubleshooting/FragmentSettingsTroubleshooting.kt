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
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.isVisible
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.databinding.FragmentSettingsTroubleshootingBinding
import com.elementary.tasks.settings.BaseSettingsFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

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

    lifecycle.addObserver(viewModel)
    viewModel.hideBatteryOptimizationCard.observe(viewLifecycleOwner) {
      binding.batterSaverOption.visibleGone(!it)
      checkIfNeedToShowEmptyState()
    }
  }

  private fun checkIfNeedToShowEmptyState() {
    if (cards().all { !it.isVisible() }) {
      binding.emptyStateView.visible()
    } else {
      binding.emptyStateView.gone()
    }
  }

  private fun cards(): List<View> {
    return listOf(
      binding.batterSaverOption
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
