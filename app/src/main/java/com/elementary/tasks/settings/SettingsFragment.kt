package com.elementary.tasks.settings

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
    binding.additionalSettings.setOnClickListener {
      safeNavigation(SettingsFragmentDirections.actionSettingsFragmentToAdditionalSettingsFragment())
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
    binding.buySettings.setOnClickListener { showProDialog() }
    if (!Module.isPro && !SuperUtil.isAppInstalled(requireContext(), "com.cray.software.justreminderpro")) {
      binding.buySettings.visible()
    } else {
      binding.buySettings.gone()
    }

    withContext {
      if (Module.hasLocation(it)) {
        binding.locationSettings.setOnClickListener {
          safeNavigation(SettingsFragmentDirections.actionSettingsFragmentToLocationSettingsFragment())
        }
        binding.locationSettings.visibility = View.VISIBLE
      } else {
        binding.locationSettings.visibility = View.GONE
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
  override fun onSale(discount: String, expiryDate: String) {
    if (dateTimeManager.isAfterNow(expiryDate)) {
      val expiry = dateTimeManager.getFireFormatted(expiryDate)
      binding.saleBadge.visibility = View.VISIBLE
      binding.saleBadge.text = "SALE" + " " + getString(R.string.app_name_pro) + " -" + discount + getString(R.string.p_until) + " " + expiry
    } else {
      binding.saleBadge.visibility = View.GONE
    }
  }

  override fun noSale() {
    binding.saleBadge.visibility = View.GONE
  }

  @SuppressLint("SetTextI18n")
  override fun onUpdate(version: String) {
    binding.updateBadge.visibility = View.VISIBLE
    binding.updateBadge.text = getString(R.string.update_available) + ": " + version
    binding.updateBadge.setOnClickListener { SuperUtil.launchMarket(requireContext()) }
  }

  override fun noUpdate() {
    binding.updateBadge.visibility = View.GONE
  }

  private fun showProDialog() {
    dialogues.getMaterialDialog(requireContext())
      .setTitle(getString(R.string.buy_pro))
      .setMessage(getString(R.string.pro_advantages) + "\n" +
        getString(R.string.different_settings_for_birthdays) + "\n" +
        "- " + getString(R.string.additional_reminder) + ";" + "\n" +
        getString(R.string._led_notification_) + "\n" +
        getString(R.string.led_color_for_each_reminder) + "\n" +
        getString(R.string.styles_for_marker) + "\n" +
        "- " + getString(R.string.no_ads))
      .setPositiveButton(R.string.buy) { dialog, _ ->
        dialog.dismiss()
        openMarket()
      }
      .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
      .setCancelable(true)
      .create().show()
  }

  private fun openMarket() {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse("market://details?id=" + "com.cray.software.justreminderpro")
    try {
      startActivity(intent)
    } catch (e: ActivityNotFoundException) {
      Toast.makeText(context, R.string.could_not_launch_market, Toast.LENGTH_SHORT).show()
    }
  }
}
