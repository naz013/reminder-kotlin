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
import com.elementary.tasks.navigation.settings.additional.AdditionalSettingsFragment
import com.elementary.tasks.navigation.settings.calendar.CalendarSettingsFragment
import com.elementary.tasks.navigation.settings.export.ExportSettingsFragment
import com.elementary.tasks.navigation.settings.location.LocationSettingsFragment
import com.elementary.tasks.navigation.settings.other.OtherSettingsFragment
import com.elementary.tasks.navigation.settings.reminders.RemindersSettingsFragment
import com.elementary.tasks.navigation.settings.security.PinLoginActivity
import com.elementary.tasks.navigation.settings.security.SecuritySettingsFragment
import com.elementary.tasks.navigation.settings.voice.VoiceSettingsFragment

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
        binding.otherSettings.setOnClickListener { callback?.openFragment(OtherSettingsFragment(), getString(R.string.other)) }
        binding.voiceSettings.setOnClickListener { callback?.openFragment(VoiceSettingsFragment(), getString(R.string.voice_control)) }
        binding.notesSettings.setOnClickListener { callback?.openFragment(NoteSettingsFragment(), getString(R.string.notes)) }
        binding.additionalSettings.setOnClickListener { callback?.openFragment(AdditionalSettingsFragment(), getString(R.string.additional)) }
        binding.notificationSettings.setOnClickListener { callback?.openFragment(NotificationSettingsFragment(), getString(R.string.notification)) }
        binding.exportSettings.setOnClickListener { callback?.openFragment(ExportSettingsFragment(), getString(R.string.export_and_sync)) }
        binding.calendarSettings.setOnClickListener { callback?.openFragment(CalendarSettingsFragment(), getString(R.string.calendar)) }
        binding.birthdaysSettings.setOnClickListener { callback?.openFragment(BirthdaySettingsFragment(), getString(R.string.birthdays)) }
        binding.remindersSettings.setOnClickListener { callback?.openFragment(RemindersSettingsFragment(), getString(R.string.reminders_)) }
        binding.securitySettings.setOnClickListener { askPin() }
        binding.testsScreen.setOnClickListener { callback?.openFragment(TestsFragment(), "Tests") }

        withContext {
            if (Module.hasLocation(it)) {
                binding.locationSettings.setOnClickListener {
                    callback?.openFragment(LocationSettingsFragment(), getString(R.string.location))
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
        callback?.openFragment(SecuritySettingsFragment(), getString(R.string.security))
    }
}
