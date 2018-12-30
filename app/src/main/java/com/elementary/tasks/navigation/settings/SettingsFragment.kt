package com.elementary.tasks.navigation.settings

import android.os.Bundle
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.navigation.settings.additional.AdditionalSettingsFragment
import com.elementary.tasks.navigation.settings.calendar.CalendarSettingsFragment
import com.elementary.tasks.navigation.settings.export.ExportSettingsFragment
import com.elementary.tasks.navigation.settings.location.LocationSettingsFragment
import com.elementary.tasks.navigation.settings.other.OtherSettingsFragment
import com.elementary.tasks.navigation.settings.security.SecuritySettingsFragment
import com.elementary.tasks.navigation.settings.voice.VoiceSettingsFragment
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
        ViewUtils.listenScrollableView(scrollView) {
            setScroll(it)
        }

        generalSettings.setOnClickListener { callback?.openFragment(GeneralSettingsFragment(), getString(R.string.general)) }
        otherSettings.setOnClickListener { callback?.openFragment(OtherSettingsFragment(), getString(R.string.other)) }
        voiceSettings.setOnClickListener { callback?.openFragment(VoiceSettingsFragment(), getString(R.string.voice_control)) }
        notesSettings.setOnClickListener { callback?.openFragment(NoteSettingsFragment(), getString(R.string.notes)) }
        locationSettings.setOnClickListener { callback?.openFragment(LocationSettingsFragment(), getString(R.string.location)) }
        additionalSettings.setOnClickListener { callback?.openFragment(AdditionalSettingsFragment(), getString(R.string.additional)) }
        notificationSettings.setOnClickListener { callback?.openFragment(NotificationSettingsFragment(), getString(R.string.notification)) }
        exportSettings.setOnClickListener { callback?.openFragment(ExportSettingsFragment(), getString(R.string.export_and_sync)) }
        calendarSettings.setOnClickListener { callback?.openFragment(CalendarSettingsFragment(), getString(R.string.calendar)) }
        birthdaysSettings.setOnClickListener { callback?.openFragment(BirthdaySettingsFragment(), getString(R.string.birthdays)) }
        securitySettings.setOnClickListener { callback?.openFragment(SecuritySettingsFragment(), getString(R.string.security)) }
        testsScreen.setOnClickListener { callback?.openFragment(TestsFragment(), "Tests") }
    }

    override fun getTitle(): String = getString(R.string.action_settings)
}
