package com.elementary.tasks.navigation.settings

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.FragmentSettingsBinding
import com.elementary.tasks.navigation.settings.additional.AdditionalSettingsFragment
import com.elementary.tasks.navigation.settings.calendar.CalendarSettingsFragment
import com.elementary.tasks.navigation.settings.export.ExportSettingsFragment
import com.elementary.tasks.navigation.settings.general.GeneralSettingsFragment
import com.elementary.tasks.navigation.settings.location.LocationSettingsFragment
import com.elementary.tasks.navigation.settings.other.OtherSettingsFragment
import com.elementary.tasks.navigation.settings.reminders.RemindersSettingsFragment
import com.elementary.tasks.navigation.settings.security.PinLoginActivity
import com.elementary.tasks.navigation.settings.security.SecuritySettingsFragment
import com.elementary.tasks.navigation.settings.voice.VoiceSettingsFragment

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class SettingsFragment : BaseSettingsFragment<FragmentSettingsBinding>() {

    override fun layoutRes(): Int = R.layout.fragment_settings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(binding.scrollView) {
            setScroll(it)
        }

        binding.generalSettings.setOnClickListener { callback?.openFragment(GeneralSettingsFragment(), getString(R.string.general)) }
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

        if (Module.hasLocation(context!!)) {
            binding.locationSettings.setOnClickListener { callback?.openFragment(LocationSettingsFragment(), getString(R.string.location)) }
            binding.locationSettings.visibility = View.VISIBLE
        } else {
            binding.locationSettings.visibility = View.GONE
        }
    }

    private fun askPin() {
        if (prefs.hasPinCode) {
            PinLoginActivity.verify(activity!!, PinLoginActivity.REQ_CODE)
        } else {
            openSecurity()
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
