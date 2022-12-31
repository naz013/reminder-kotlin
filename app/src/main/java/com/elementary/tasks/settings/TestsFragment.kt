package com.elementary.tasks.settings

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.birthdays.preview.ShowBirthday29Activity
import com.elementary.tasks.birthdays.preview.ShowBirthdayActivity
import com.elementary.tasks.core.additional.FollowReminderActivity
import com.elementary.tasks.sms.QuickSmsActivity
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.MissedCall
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.databinding.FragmentSettingsTestsBinding
import com.elementary.tasks.missed_calls.MissedCallDialog29Activity
import com.elementary.tasks.missed_calls.MissedCallDialogActivity
import com.elementary.tasks.reminder.preview.ReminderDialog29Activity
import com.elementary.tasks.reminder.preview.ReminderDialogActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class TestsFragment : BaseSettingsFragment<FragmentSettingsTestsBinding>() {

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsTestsBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.birthdayDialogWindow.setOnClickListener { openBirthdayScreen() }
    binding.reminderDialogWindow.setOnClickListener { openReminderScreen() }
    binding.missedCallWindow.setOnClickListener { openMissedScreen() }

    binding.quickSmsWindow.setOnClickListener { QuickSmsActivity.openScreen(requireContext(), "2454548") }

    binding.afterCallWindow.setOnClickListener {
      FollowReminderActivity.mockScreen(requireContext(), "2454548", System.currentTimeMillis())
    }
  }

  private fun openMissedScreen() {
    MissedCall(number = "2454548", dateTime = System.currentTimeMillis()).also {
      if (Module.is10) {
        MissedCallDialog29Activity.mockTest(requireContext(), it)
      } else {
        MissedCallDialogActivity.mockTest(requireContext(), it)
      }
    }
  }

  private fun openReminderScreen() {
    Reminder().apply {
      this.summary = "Test"
      this.target = "16546848"
      this.type = Reminder.BY_DATE_CALL
      this.useGlobal = true
    }.also {
      if (Module.is10) {
        ReminderDialog29Activity.mockTest(requireContext(), it)
      } else {
        ReminderDialogActivity.mockTest(requireContext(), it)
      }
    }
  }

  private fun openBirthdayScreen() {
    Birthday().apply {
      this.day = 25
      this.month = 5
      this.name = "Test User"
      this.showedYear = 2017
      this.uniqueId = 12123
      this.uuId = UUID.randomUUID().toString()
      this.number = "16546848"
      this.date = createBirthDate(day, month, 1955)

      val secKey = if (TextUtils.isEmpty(number)) "0" else number.substring(1)
      this.key = "$name|$secKey"

      this.dayMonth = "$day|$month"
    }.also {
      if (Module.is10) {
        ShowBirthday29Activity.mockTest(requireContext(), it)
      } else {
        ShowBirthdayActivity.mockTest(requireContext(), it)
      }
    }
  }

  private fun createBirthDate(day: Int, month: Int, year: Int): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = System.currentTimeMillis()
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month)
    calendar.set(Calendar.DAY_OF_MONTH, day)
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
  }

  override fun getTitle(): String = "Tests"
}
