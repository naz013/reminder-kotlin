package com.elementary.tasks.navigation.settings

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.createEdit.AddBirthdayActivity
import com.elementary.tasks.birthdays.preview.ShowBirthdayActivity
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.reminder.preview.ReminderDialogActivity
import kotlinx.android.synthetic.main.fragment_settings_tests.*
import java.util.*

/**
 * Copyright 2018 Nazar Suhovich
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
class TestsFragment : BaseSettingsFragment() {

    override fun layoutRes(): Int = R.layout.fragment_settings_tests

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        birthdayDialogWindow.setOnClickListener { openBirthdayScreen() }
        reminderDialogWindow.setOnClickListener { openReminderScreen() }
    }

    private fun openReminderScreen() {
        val reminder = Reminder().apply {
            this.summary = "Test"
            this.target = "16546848"
            this.type = Reminder.BY_DATE_CALL
            this.useGlobal = true
        }
        ReminderDialogActivity.mockTest(context!!, reminder)
    }

    private fun openBirthdayScreen() {
        val birthday = Birthday().apply {
            this.day = 25
            this.month = 5
            this.name = "Test User"
            this.showedYear = 2017
            this.uniqueId = 12123
            this.uuId = UUID.randomUUID().toString()
            this.number = "16546848"
            this.date = AddBirthdayActivity.createBirthDate(day, month, 1955)

            val secKey = if (TextUtils.isEmpty(number)) "0" else number.substring(1)
            this.key = "$name|$secKey"

            this.dayMonth = day.toString() + "|" + month
        }
        ShowBirthdayActivity.mockTest(context!!, birthday)
    }

    override fun getTitle(): String = "Tests"
}
