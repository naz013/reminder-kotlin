package com.elementary.tasks.settings.test

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.reminder.ShopItem
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime

class TestsSettingsViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val reminderRepository: ReminderRepository,
  private val birthdayRepository: BirthdayRepository,
  private val dateTimeManager: DateTimeManager,
) : ViewModel() {

  val state: StateFlow<TestsSettingsState> field = MutableStateFlow(TestsSettingsState())
  val navigationEvent: LiveData<Event<TestsSettingsEvent>> field = mutableLiveEventOf()

  fun onReminderDialogClick() {
    state.update {
      it.copy(
        dialog = TestChoiceDialog(kind = TestDialogKind.REMINDER, options = REMINDER_OPTIONS, selectedIndex = 0),
      )
    }
  }

  fun onBirthdayDialogClick() {
    state.update {
      it.copy(
        dialog = TestChoiceDialog(kind = TestDialogKind.BIRTHDAY, options = BIRTHDAY_OPTIONS, selectedIndex = 0),
      )
    }
  }

  fun onDialogOptionSelected(index: Int) {
    state.update { current ->
      val dialog = current.dialog ?: return@update current
      current.copy(dialog = dialog.copy(selectedIndex = index))
    }
  }

  fun onDialogConfirm() {
    val dialog = state.value.dialog ?: return
    when (dialog.kind) {
      TestDialogKind.REMINDER -> saveAndOpenReminder(dialog.selectedIndex)
      TestDialogKind.BIRTHDAY -> saveAndOpenBirthday(dialog.selectedIndex)
    }
    dismissDialog()
  }

  fun onDialogDismiss() {
    dismissDialog()
  }

  fun onObjectExportClick() {
    navigationEvent.value = Event(TestsSettingsEvent.OpenObjectExport)
  }

  fun onDeveloperOptionsClick() {
    navigationEvent.value = Event(TestsSettingsEvent.OpenDeveloperOptions)
  }

  fun onReviewDialogClick() {
    navigationEvent.value = Event(TestsSettingsEvent.OpenReviewDialog)
  }

  private fun dismissDialog() {
    state.update { it.copy(dialog = null) }
  }

  private fun saveAndOpenReminder(selectedItem: Int) {
    val reminder = prepareReminder(selectedItem)
    viewModelScope.launch(dispatcherProvider.io()) {
      reminderRepository.save(reminder)
      navigationEvent.postValue(Event(TestsSettingsEvent.OpenReminderAction(reminder.uuId)))
    }
  }

  private fun saveAndOpenBirthday(selectedItem: Int) {
    val birthday = prepareBirthday(selectedItem)
    viewModelScope.launch(dispatcherProvider.io()) {
      birthdayRepository.save(birthday)
      navigationEvent.postValue(Event(TestsSettingsEvent.OpenBirthdayAction(birthday.uuId)))
    }
  }

  private fun prepareReminder(selectedItem: Int): Reminder {
    val reminder = Reminder()
    reminder.eventTime = dateTimeManager.getGmtFromDateTime(LocalDateTime.now())
    when (selectedItem) {
      0 -> reminder.summary = "This is a simple reminder."

      1 -> {
        reminder.summary = "This is a recurring daily reminder."
        reminder.repeatInterval = 24 * 60 * 60 * 1000L
      }

      2 -> {
        reminder.summary = "This is a reminder with todo."
        reminder.shoppings = listOf(
          ShopItem(summary = "Milk", createTime = "", isChecked = false),
          ShopItem(summary = "Bread", createTime = "", isChecked = false),
          ShopItem(summary = "Eggs", createTime = "", isChecked = false),
          ShopItem(summary = "Butter", createTime = "", isChecked = true),
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

  private fun prepareBirthday(selectedItem: Int): Birthday = when (selectedItem) {
    0 -> Birthday(name = "John Doe", date = "1990-05-15", number = "", syncState = SyncState.Synced)
    1 -> Birthday(name = "Jane Smith", date = "1985-10-20", number = "+1234567890", syncState = SyncState.Synced)
    2 -> Birthday(
      name = "Alice Johnson",
      date = "2000-07-25",
      number = "",
      ignoreYear = true,
      syncState = SyncState.Synced,
    )

    else -> Birthday(syncState = SyncState.Synced)
  }

  companion object {
    private val REMINDER_OPTIONS = listOf(
      "Simple reminder",
      "Recurring reminder",
      "With Todo",
      "With Call action",
      "With SMS action",
      "With Email action",
      "With Open URL action",
      "With Open Chrome Browser action",
    )
    private val BIRTHDAY_OPTIONS = listOf(
      "Simple birthday",
      "Birthday with number",
      "Birthday without age",
    )
  }
}
