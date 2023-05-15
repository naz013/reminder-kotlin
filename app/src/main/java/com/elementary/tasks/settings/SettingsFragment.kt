package com.elementary.tasks.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.os.datapicker.LoginLauncher
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.datetime.DoNotDisturbManager
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.params.PrefsConstants
import com.elementary.tasks.core.utils.params.RemotePrefs
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.databinding.FragmentSettingsBinding
import org.koin.android.ext.android.inject
import timber.log.Timber

class SettingsFragment : BaseSettingsFragment<FragmentSettingsBinding>(),
  RemotePrefs.SaleObserver, RemotePrefs.UpdateObserver {

  private val remotePrefs: RemotePrefs by inject()
  private val doNotDisturbManager by inject<DoNotDisturbManager>()
  private val dateTimeManager by inject<DateTimeManager>()

  private val prefsObserver: (String) -> Unit = {
    Handler(Looper.getMainLooper()).post {
      if (it == PrefsConstants.DATA_BACKUP) {
        checkBackupPrefs()
      } else {
        checkDoNotDisturb()
      }
    }
  }
  private val loginLauncher = LoginLauncher(this) {
    if (it) openSecurity()
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsBinding.inflate(inflater, container, false)

  override fun onResume() {
    super.onResume()
    prefs.addObserver(PrefsConstants.DO_NOT_DISTURB_ENABLED, prefsObserver)
    prefs.addObserver(PrefsConstants.DO_NOT_DISTURB_FROM, prefsObserver)
    prefs.addObserver(PrefsConstants.DO_NOT_DISTURB_TO, prefsObserver)
    prefs.addObserver(PrefsConstants.DO_NOT_DISTURB_IGNORE, prefsObserver)
    prefs.addObserver(PrefsConstants.DATA_BACKUP, prefsObserver)
    remotePrefs.addUpdateObserver(this)
    if (!Module.isPro) {
      remotePrefs.addSaleObserver(this)
    }
    checkDoNotDisturb()
    checkBackupPrefs()
  }

  override fun onPause() {
    super.onPause()
    prefs.removeObserver(PrefsConstants.DATA_BACKUP, prefsObserver)
    prefs.removeObserver(PrefsConstants.DO_NOT_DISTURB_ENABLED, prefsObserver)
    prefs.removeObserver(PrefsConstants.DO_NOT_DISTURB_FROM, prefsObserver)
    prefs.removeObserver(PrefsConstants.DO_NOT_DISTURB_TO, prefsObserver)
    prefs.removeObserver(PrefsConstants.DO_NOT_DISTURB_IGNORE, prefsObserver)
    if (!Module.isPro) {
      remotePrefs.removeSaleObserver(this)
    }
    remotePrefs.removeUpdateObserver(this)
  }

  private fun checkBackupPrefs() {
    if (prefs.isBackupEnabled) {
      binding.backupBadge.gone()
    } else {
      binding.backupBadge.visible()
    }
  }

  private fun checkDoNotDisturb() {
    if (doNotDisturbManager.applyDoNotDisturb(0)) {
      Timber.d("checkDoNotDisturb: active")
      binding.doNoDisturbIcon.visible()
    } else {
      Timber.d("checkDoNotDisturb: not active")
      binding.doNoDisturbIcon.gone()
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.saleBadge.gone()
    binding.updateBadge.gone()
    binding.doNoDisturbIcon.gone()
    if (Module.isPro) {
      binding.appNameBannerPro.visible()
    } else {
      binding.appNameBannerPro.gone()
    }
    if (SuperUtil.isGooglePlayServicesAvailable(requireContext())) {
      binding.playServicesWarning.gone()
    } else {
      binding.playServicesWarning.visible()
    }

    binding.generalSettings.setOnClickListener {
      safeNavigation(SettingsFragmentDirections.actionSettingsFragmentToGeneralSettingsFragment())
    }
    binding.otherSettings.setOnClickListener {
      safeNavigation(SettingsFragmentDirections.actionSettingsFragmentToOtherSettingsFragment())
    }
    binding.voiceSettings.setOnClickListener {
      safeNavigation(SettingsFragmentDirections.actionSettingsFragmentToVoiceSettingsFragment())
    }
    binding.notesSettings.setOnClickListener {
      safeNavigation(SettingsFragmentDirections.actionSettingsFragmentToNoteSettingsFragment())
    }
    binding.notificationSettings.setOnClickListener {
      safeNavigation(SettingsFragmentDirections.actionSettingsFragmentToNotificationSettingsFragment())
    }
    binding.exportSettings.setOnClickListener {
      safeNavigation(SettingsFragmentDirections.actionSettingsFragmentToExportSettingsFragment())
    }
    binding.calendarSettings.setOnClickListener {
      safeNavigation(SettingsFragmentDirections.actionSettingsFragmentToCalendarSettingsFragment())
    }
    binding.birthdaysSettings.setOnClickListener {
      safeNavigation(SettingsFragmentDirections.actionSettingsFragmentToBirthdaySettingsFragment())
    }
    binding.remindersSettings.setOnClickListener {
      safeNavigation(SettingsFragmentDirections.actionSettingsFragmentToRemindersSettingsFragment())
    }
    binding.securitySettings.setOnClickListener { askPin() }
    binding.testsScreen.setOnClickListener {
      safeNavigation(SettingsFragmentDirections.actionSettingsFragmentToTestsFragment())
    }
    binding.buyProBadge.setOnClickListener { openProPage() }
    if (!Module.isPro && !SuperUtil.isAppInstalled(
        requireContext(),
        "com.cray.software.justreminderpro"
      )
    ) {
      binding.buyProBadge.visible()
    } else {
      binding.buyProBadge.gone()
    }

    withContext {
      if (Module.hasLocation(it)) {
        binding.locationSettings.setOnClickListener {
          safeNavigation(SettingsFragmentDirections.actionSettingsFragmentToLocationSettingsFragment())
        }
        binding.locationSettings.visible()
      } else {
        binding.locationSettings.gone()
      }
    }
  }

  private fun askPin() {
    withActivity {
      if (prefs.hasPinCode) {
        loginLauncher.askLogin()
      } else {
        openSecurity()
      }
    }
  }

  override fun getTitle(): String = getString(R.string.action_settings)

  private fun openSecurity() {
    safeNavigation(SettingsFragmentDirections.actionSettingsFragmentToSecuritySettingsFragment())
  }

  @SuppressLint("SetTextI18n")
  override fun onUpdateChanged(hasUpdate: Boolean, version: String) {
    Timber.d("onUpdateChanged: $hasUpdate, $version")
    if (hasUpdate) {
      binding.updateBadge.visible()
      binding.updateBadge.text = getString(R.string.new_update_message, version)
      binding.updateBadge.setOnClickListener { SuperUtil.launchMarket(requireContext()) }
    } else {
      binding.updateBadge.gone()
    }
  }

  override fun onSaleChanged(showDiscount: Boolean, discount: String, until: String) {
    Timber.d("onSaleChanged: $showDiscount, $discount")
    if (showDiscount) {
      binding.saleBadge.visible()
      binding.saleBadge.text = getString(R.string.new_sale_message, discount, until)
    } else {
      binding.saleBadge.gone()
    }
  }

  private fun openProPage() {
    safeNavigation(SettingsFragmentDirections.actionSettingsFragmentToFragmentProVersion())
  }
}
