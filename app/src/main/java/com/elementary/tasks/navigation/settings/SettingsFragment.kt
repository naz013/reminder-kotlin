package com.elementary.tasks.navigation.settings

import android.os.Bundle
import android.view.View
import com.elementary.tasks.R
import com.mcxiaoke.koi.ext.onClick
import kotlinx.android.synthetic.main.fragment_settings.*

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

    override fun layoutRes(): Int = R.layout.fragment_settings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        generalSettings.onClick { replaceFragment(GeneralSettingsFragment(), getString(R.string.general)) }
        otherSettings.onClick { replaceFragment(OtherSettingsFragment(), getString(R.string.other)) }
        voiceSettings.onClick { replaceFragment(VoiceSettingsFragment(), getString(R.string.voice_control)) }
        notesSettings.onClick { replaceFragment(NoteSettingsFragment(), getString(R.string.notes)) }
        locationSettings.onClick { replaceFragment(LocationSettingsFragment(), getString(R.string.location)) }
        additionalSettings.onClick { replaceFragment(AdditionalSettingsFragment(), getString(R.string.additional)) }
        notificationSettings.onClick { replaceFragment(NotificationSettingsFragment(), getString(R.string.notification)) }
        exportSettings.onClick { replaceFragment(ExportSettingsFragment(), getString(R.string.export_and_sync)) }
        calendarSettings.onClick { replaceFragment(CalendarSettingsFragment(), getString(R.string.calendar)) }
        birthdaysSettings.onClick { replaceFragment(BirthdaySettingsFragment(), getString(R.string.birthdays)) }
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback?.onTitleChange(getString(R.string.action_settings))
            callback?.onFragmentSelect(this)
        }
    }
}
