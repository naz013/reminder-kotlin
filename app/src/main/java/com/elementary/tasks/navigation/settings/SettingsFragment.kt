package com.elementary.tasks.navigation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.elementary.tasks.R
import com.elementary.tasks.databinding.FragmentSettingsBinding

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

class SettingsFragment : BaseSettingsFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentSettingsBinding.inflate(inflater, container, false)
        binding.generalSettings.setOnClickListener { view -> replaceFragment(GeneralSettingsFragment(), getString(R.string.general)) }
        binding.otherSettings.setOnClickListener { view -> replaceFragment(OtherSettingsFragment(), getString(R.string.other)) }
        binding.voiceSettings.setOnClickListener { view -> replaceFragment(VoiceSettingsFragment(), getString(R.string.voice_control)) }
        binding.notesSettings.setOnClickListener { view -> replaceFragment(NoteSettingsFragment(), getString(R.string.notes)) }
        binding.locationSettings.setOnClickListener { view -> replaceFragment(LocationSettingsFragment(), getString(R.string.location)) }
        binding.additionalSettings.setOnClickListener { view -> replaceFragment(AdditionalSettingsFragment(), getString(R.string.additional)) }
        binding.notificationSettings.setOnClickListener { view -> replaceFragment(NotificationSettingsFragment(), getString(R.string.notification)) }
        binding.exportSettings.setOnClickListener { view -> replaceFragment(ExportSettingsFragment(), getString(R.string.export_and_sync)) }
        binding.calendarSettings.setOnClickListener { view -> replaceFragment(CalendarSettingsFragment(), getString(R.string.calendar)) }
        binding.birthdaysSettings.setOnClickListener { view -> replaceFragment(BirthdaySettingsFragment(), getString(R.string.birthdays)) }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback!!.onTitleChange(getString(R.string.action_settings))
            callback!!.onFragmentSelect(this)
            callback!!.setClick(null)
        }
    }
}
