package com.elementary.tasks.settings.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.elementary.tasks.birthdays.dialog.ShowBirthday29Activity
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.databinding.FragmentSettingsTestsBinding
import com.elementary.tasks.navigation.fragments.BaseSettingsFragment
import com.elementary.tasks.reminder.dialog.ReminderActionActivity
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.reminder.ShopItem
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.reviews.AppSource
import com.github.naz013.reviews.ReviewsApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.threeten.bp.LocalDateTime

class TestsFragment : BaseSettingsFragment<FragmentSettingsTestsBinding>() {

  private val reviewsApi by inject<ReviewsApi>()
  private val reminderRepository by inject<ReminderRepository>()
  private val dateTimeManager by inject<DateTimeManager>()

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsTestsBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.birthdayDialogWindow.setOnClickListener { openBirthdayScreen(true) }
    binding.birthdayNoNumber.setOnClickListener { openBirthdayScreen() }
    binding.reminderDialogWindow.setOnClickListener { showReminderActionSelectionDialog() }
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

  private fun showReminderActionSelectionDialog() {
    var selectedItem = 0
    dialogues.getMaterialDialog(requireContext())
      .setTitle("Select action to test")
      .setSingleChoiceItems(
        arrayOf(
          "Simple reminder",
          "Recurring reminder",
          "With Todo",
          "With Call action",
          "With SMS action",
          "With Email action",
          "With Open URL action",
          "With Open Chrome Browser action"
        ),
        selectedItem,
        { _, which ->
          selectedItem = which
        }
      )
      .setPositiveButton("Run") { dialog, _ ->
        dialog.dismiss()
        saveAndOpenReminderScreen(prepareReminder(selectedItem))
      }
      .setNegativeButton("Cancel") { dialog, _ ->
        dialog.dismiss()
      }
      .show()
  }

  private fun prepareReminder(selectedItem: Int): Reminder {
    val reminder = Reminder()
    reminder.eventTime = dateTimeManager.getGmtFromDateTime(LocalDateTime.now())
    when (selectedItem) {
      0 -> {
        reminder.summary = "This is a simple reminder."
      }
      1 -> {
        reminder.summary = "This is a recurring daily reminder."
        reminder.repeatInterval = 24 * 60 * 60 * 1000L //daily
      }
      2 -> {
        reminder.summary = "This is a reminder with todo."
        reminder.shoppings = listOf(
          ShopItem(summary = "Milk", createTime = "", isChecked = false),
          ShopItem(summary = "Bread", createTime = "", isChecked = false),
          ShopItem(summary = "Eggs", createTime = "", isChecked = false),
          ShopItem(summary = "Butter", createTime = "", isChecked = true)
        )
      }
      3 -> {
        reminder.summary = "This is a reminder with call action."
        reminder.type = 10 + Reminder.Action.CALL
        reminder.target = "+1234567890"
      }
      4 -> {
        reminder.summary = "This is a reminder with SMS action."
        reminder.type = 10 + Reminder.Action.SMS
        reminder.target = "+1234567890"
      }
      5 -> {
        reminder.type = 10 + Reminder.Action.EMAIL
        reminder.target = "some@mail.com"
        reminder.subject = "Test Subject"
        reminder.summary = "This is a test email from Tasks app."
      }
      6 -> {
        reminder.summary = "This is a reminder with open link action."
        reminder.type = 10 + Reminder.Action.LINK
        reminder.target = "https://www.google.com"
      }
      7 -> {
        reminder.summary = "This is a reminder with open Chrome action."
        reminder.type = 10 + Reminder.Action.APP
        reminder.target = "com.android.chrome"
      }
    }
    return reminder
  }

  private fun saveAndOpenReminderScreen(reminder: Reminder) {
    Logger.d("TestsFragment", "Saving reminder and opening action screen...")
    lifecycleScope.launch(Dispatchers.IO) {
      reminderRepository.save(reminder)
      withContext(Dispatchers.Main) {
        openReminderScreen(reminder)
      }
    }
  }

  private fun openReminderScreen(reminder: Reminder) {
    Logger.d("TestsFragment", "Opening reminder action screen for id: ${reminder.uuId}")
    ReminderActionActivity.mockTest(requireContext(), reminder.uuId)
  }

  private fun openBirthdayScreen(hasNumber: Boolean = false) {
    ShowBirthday29Activity.mockTest(requireContext(), hasNumber)
  }

  override fun getTitle(): String = "Tests"
}
