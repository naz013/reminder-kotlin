package com.elementary.tasks.settings.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.birthdays.dialog.ShowBirthday29Activity
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.databinding.FragmentSettingsTestsBinding
import com.elementary.tasks.navigation.fragments.BaseSettingsFragment
import com.elementary.tasks.reminder.dialog.ReminderDialog29Activity
import com.github.naz013.domain.Reminder
import com.github.naz013.reviews.AppSource
import com.github.naz013.reviews.ReviewsApi
import org.koin.android.ext.android.inject

class TestsFragment : BaseSettingsFragment<FragmentSettingsTestsBinding>() {

  private val reviewsApi by inject<ReviewsApi>()

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsTestsBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.birthdayDialogWindow.setOnClickListener { openBirthdayScreen(true) }
    binding.birthdayNoNumber.setOnClickListener { openBirthdayScreen() }
    binding.reminderDialogWindow.setOnClickListener { openReminderScreen() }
    binding.objectExport.setOnClickListener {
      safeNavigation(TestsFragmentDirections.actionTestsFragmentToObjectExportTestFragment())
    }
    binding.reviewDialogTest.setOnClickListener {
      reviewsApi.showFeedbackForm(
        context = requireContext(),
        title = "Write a review",
        appSource = if (BuildParams.isPro) {
          AppSource.PRO
        } else {
          AppSource.FREE
        },
        allowLogsAttachment = false
      )
    }
  }

  private fun openReminderScreen() {
    Reminder().apply {
      this.summary = "Test"
      this.target = "16546848"
      this.type = Reminder.BY_DATE + 1
      this.useGlobal = true
    }.also {
      ReminderDialog29Activity.mockTest(requireContext(), it)
    }
  }

  private fun openBirthdayScreen(hasNumber: Boolean = false) {
    ShowBirthday29Activity.mockTest(requireContext(), hasNumber)
  }

  override fun getTitle(): String = "Tests"
}
