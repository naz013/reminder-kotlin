package com.elementary.tasks.navigation.settings

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.FragmentSettingsBinding
import com.elementary.tasks.navigation.settings.security.PinLoginActivity

class SettingsFragment : BaseSettingsFragment<FragmentSettingsBinding>() {

    override fun layoutRes(): Int = R.layout.fragment_settings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(binding.scrollView) {
            setScroll(it)
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
}
