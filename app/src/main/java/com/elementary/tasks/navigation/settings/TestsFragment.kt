package com.elementary.tasks.navigation.settings

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.create.AddBirthdayActivity
import com.elementary.tasks.birthdays.preview.ShowBirthdayActivity
import com.elementary.tasks.core.additional.FollowReminderActivity
import com.elementary.tasks.core.additional.QuickSmsActivity
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.MissedCall
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.databinding.FragmentSettingsTestsBinding
import com.elementary.tasks.missed_calls.MissedCallDialogActivity
import com.elementary.tasks.reminder.preview.ReminderDialogActivity
import java.util.*

class TestsFragment : BaseSettingsFragment<FragmentSettingsTestsBinding>() {

    override fun layoutRes(): Int = R.layout.fragment_settings_tests

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.birthdayDialogWindow.setOnClickListener { openBirthdayScreen() }
        binding.reminderDialogWindow.setOnClickListener { openReminderScreen() }

        binding.missedCallWindow.setOnClickListener {
            MissedCallDialogActivity.mockTest(context!!,
                    MissedCall(number = "2454548", dateTime = System.currentTimeMillis()))
        }

        binding.quickSmsWindow.setOnClickListener {
            QuickSmsActivity.openScreen(context!!, "2454548")
        }

        binding.afterCallWindow.setOnClickListener {
            FollowReminderActivity.mockScreen(context!!, "2454548", System.currentTimeMillis())
        }
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

            this.dayMonth = "$day|$month"
        }
        ShowBirthdayActivity.mockTest(context!!, birthday)
    }

    override fun getTitle(): String = "Tests"
}
