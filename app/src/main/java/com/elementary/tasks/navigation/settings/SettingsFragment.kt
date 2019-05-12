package com.elementary.tasks.navigation.settings

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import androidx.navigation.fragment.findNavController
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.databinding.FragmentSettingsBinding
import com.elementary.tasks.navigation.settings.security.PinLoginActivity
import timber.log.Timber

class SettingsFragment : BaseSettingsFragment<FragmentSettingsBinding>(), RemotePrefs.SaleObserver, RemotePrefs.UpdateObserver {

    private lateinit var remotePrefs: RemotePrefs
    private val prefsObserver: (String) -> Unit = {
        Handler(Looper.getMainLooper()).post {
            if (it == PrefsConstants.DATA_BACKUP) {
                checkBackupPrefs()
            } else {
                checkDoNotDisturb()
            }
        }
    }

    override fun layoutRes(): Int = R.layout.fragment_settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        remotePrefs = RemotePrefs(context!!)
    }

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
            binding.backupBadge.hide()
        } else {
            binding.backupBadge.show()
        }
    }

    private fun checkDoNotDisturb() {
        if (prefs.applyDoNotDisturb(0)) {
            Timber.d("checkDoNotDisturb: active")
            binding.doNoDisturbIcon.show()
        } else {
            Timber.d("checkDoNotDisturb: not active")
            binding.doNoDisturbIcon.hide()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(binding.scrollView) {
            setScroll(it)
        }

        binding.saleBadge.hide()
        binding.updateBadge.hide()
        binding.doNoDisturbIcon.hide()
        if (Module.isPro) {
            binding.appNameBannerPro.show()
        } else {
            binding.appNameBannerPro.hide()
        }
        if (SuperUtil.isGooglePlayServicesAvailable(activity!!)) {
            binding.playServicesWarning.hide()
        } else {
            binding.playServicesWarning.show()
        }

        binding.generalSettings.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToGeneralSettingsFragment())
        }
        binding.otherSettings.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToOtherSettingsFragment())
        }
        binding.voiceSettings.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToVoiceSettingsFragment())
        }
        binding.notesSettings.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToNoteSettingsFragment())
        }
        binding.additionalSettings.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToAdditionalSettingsFragment())
        }
        binding.notificationSettings.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToNotificationSettingsFragment())
        }
        binding.exportSettings.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToExportSettingsFragment())
        }
        binding.calendarSettings.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToCalendarSettingsFragment())
        }
        binding.birthdaysSettings.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToBirthdaySettingsFragment())
        }
        binding.remindersSettings.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToRemindersSettingsFragment())
        }
        binding.securitySettings.setOnClickListener { askPin() }
        binding.testsScreen.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToTestsFragment())
        }

        withContext {
            if (Module.hasLocation(it)) {
                binding.locationSettings.setOnClickListener {
                    findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToLocationSettingsFragment())
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
                PinLoginActivity.verify(it, PinLoginActivity.REQ_CODE)
            } else {
                openSecurity()
            }
        }
    }

    override fun getTitle(): String = getString(R.string.action_settings)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PinLoginActivity.REQ_CODE && resultCode == RESULT_OK) {
            openSecurity()
        }
    }

    private fun openSecurity() {
        findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToSecuritySettingsFragment())
    }

    override fun onSale(discount: String, expiryDate: String) {
        val expiry = TimeUtil.getFireFormatted(prefs, expiryDate)
        if (TextUtils.isEmpty(expiry)) {
            binding.saleBadge.visibility = View.GONE
        } else {
            binding.saleBadge.visibility = View.VISIBLE
            binding.saleBadge.text = "SALE" + " " + getString(R.string.app_name_pro) + " -" + discount + getString(R.string.p_until) + " " + expiry
        }
    }

    override fun noSale() {
        binding.saleBadge.visibility = View.GONE
    }

    override fun onUpdate(version: String) {
        binding.updateBadge.visibility = View.VISIBLE
        binding.updateBadge.text = getString(R.string.update_available) + ": " + version
        binding.updateBadge.setOnClickListener { SuperUtil.launchMarket(context!!) }
    }

    override fun noUpdate() {
        binding.updateBadge.visibility = View.GONE
    }
}
