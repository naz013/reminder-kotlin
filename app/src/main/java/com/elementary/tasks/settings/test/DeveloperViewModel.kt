package com.elementary.tasks.settings.test

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.reminder.ShopItem
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.legal.LegalDocumentRepository
import com.github.naz013.legal.LegalDocumentType
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.CalendarEventRepository
import com.github.naz013.repository.EventHistoryRepository
import com.github.naz013.repository.EventOccurrenceRepository
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.PlaceRepository
import com.github.naz013.repository.RecentQueryRepository
import com.github.naz013.repository.RecurPresetRepository
import com.github.naz013.repository.ReminderGroupRepository
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.repository.RemoteFileMetadataRepository
import com.github.naz013.repository.UsedTimeRepository
import com.github.naz013.repository.table.Table
import com.github.naz013.ui.common.theme.ThemeProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class DeveloperViewModel(
  private val legalDocumentRepository: LegalDocumentRepository,
  private val prefs: Prefs,
  private val dispatcherProvider: DispatcherProvider,
  private val reminderRepository: ReminderRepository,
  private val birthdayRepository: BirthdayRepository,
  private val dateTimeManager: DateTimeManager,
  private val calendarEventRepository: CalendarEventRepository,
  private val eventHistoryRepository: EventHistoryRepository,
  private val eventOccurrenceRepository: EventOccurrenceRepository,
  private val googleTaskListRepository: GoogleTaskListRepository,
  private val googleTaskRepository: GoogleTaskRepository,
  private val noteRepository: NoteRepository,
  private val placeRepository: PlaceRepository,
  private val recentQueryRepository: RecentQueryRepository,
  private val recurPresetRepository: RecurPresetRepository,
  private val reminderGroupRepository: ReminderGroupRepository,
  private val remoteFileMetadataRepository: RemoteFileMetadataRepository,
  private val usedTimeRepository: UsedTimeRepository,
) : ViewModel() {

  val state: StateFlow<DeveloperState> field = MutableStateFlow(DeveloperState())
  val bannersReset: LiveData<Event<Unit>> field = mutableLiveEventOf()
  val actionMessage: LiveData<Event<String>> field = mutableLiveEventOf()
  val navigationEvent: LiveData<Event<DeveloperEvent>> field = mutableLiveEventOf()

  fun onResetBannersClick() {
    legalDocumentRepository.resetSeen(LegalDocumentType.PRIVACY_POLICY)
    prefs.isUserLogged = false
    prefs.lastVersionCode = 0
    bannersReset.value = Event(Unit)
  }

  fun onReminderDialogClick() {
    state.update {
      it.copy(
        dialog = DeveloperChoiceDialog(kind = DeveloperDialogKind.REMINDER, options = REMINDER_OPTIONS, selectedIndex = 0),
      )
    }
  }

  fun onBirthdayDialogClick() {
    state.update {
      it.copy(
        dialog = DeveloperChoiceDialog(kind = DeveloperDialogKind.BIRTHDAY, options = BIRTHDAY_OPTIONS, selectedIndex = 0),
      )
    }
  }

  fun onClearTableClick() {
    state.update {
      it.copy(
        dialog = DeveloperChoiceDialog(kind = DeveloperDialogKind.CLEAR_TABLE, options = TABLE_OPTIONS, selectedIndex = 0),
      )
    }
  }

  fun onClearAllTablesClick() {
    state.update { it.copy(clearAllTablesConfirmation = true) }
  }

  fun onClearAllTablesConfirm() {
    state.update { it.copy(clearAllTablesConfirmation = false) }
    viewModelScope.launch(dispatcherProvider.io()) {
      Table.entries.forEach { clearTable(it) }
      actionMessage.postValue(Event("All tables have been cleared"))
    }
  }

  fun onClearAllTablesDismiss() {
    state.update { it.copy(clearAllTablesConfirmation = false) }
  }

  fun onInsertDemoDataClick() {
    viewModelScope.launch(dispatcherProvider.io()) {
      insertDemoReminders()
      insertDemoBirthdays()
      insertDemoNotes()
      actionMessage.postValue(Event("Demo data has been inserted"))
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
      DeveloperDialogKind.REMINDER -> saveAndOpenReminder(dialog.selectedIndex)
      DeveloperDialogKind.BIRTHDAY -> saveAndOpenBirthday(dialog.selectedIndex)
      DeveloperDialogKind.CLEAR_TABLE -> clearSelectedTable(dialog.selectedIndex)
    }
    dismissDialog()
  }

  fun onDialogDismiss() {
    dismissDialog()
  }

  fun onObjectExportClick() {
    navigationEvent.value = Event(DeveloperEvent.OpenObjectExport)
  }

  fun onReviewDialogClick() {
    navigationEvent.value = Event(DeveloperEvent.OpenReviewDialog)
  }

  fun onProVersionClick() {
    navigationEvent.value = Event(DeveloperEvent.OpenProVersion)
  }

  private fun dismissDialog() {
    state.update { it.copy(dialog = null) }
  }

  private fun saveAndOpenReminder(selectedItem: Int) {
    val reminder = prepareReminder(selectedItem)
    viewModelScope.launch(dispatcherProvider.io()) {
      reminderRepository.save(reminder)
      navigationEvent.postValue(Event(DeveloperEvent.OpenReminderAction(reminder.uuId)))
    }
  }

  private fun saveAndOpenBirthday(selectedItem: Int) {
    val birthday = prepareBirthday(selectedItem)
    viewModelScope.launch(dispatcherProvider.io()) {
      birthdayRepository.save(birthday)
      navigationEvent.postValue(Event(DeveloperEvent.OpenBirthdayAction(birthday.uuId)))
    }
  }

  private fun clearSelectedTable(selectedIndex: Int) {
    val table = Table.entries[selectedIndex]
    viewModelScope.launch(dispatcherProvider.io()) {
      clearTable(table)
      actionMessage.postValue(Event("${table.tableName} table has been cleared"))
    }
  }

  private suspend fun clearTable(table: Table) {
    when (table) {
      Table.Birthday -> birthdayRepository.deleteAll()
      Table.RecentQuery -> recentQueryRepository.deleteAll()
      Table.RecurPreset -> recurPresetRepository.deleteAll()
      Table.UsedTime -> usedTimeRepository.deleteAll()
      Table.CalendarEvent -> calendarEventRepository.deleteAll()
      Table.ReminderGroup -> reminderGroupRepository.deleteAll()
      Table.Reminder -> reminderRepository.deleteAll()
      Table.Place -> placeRepository.deleteAll()
      Table.Note -> noteRepository.deleteAllNotes()
      Table.ImageFile -> noteRepository.deleteAllImages()
      Table.GoogleTaskList -> googleTaskListRepository.deleteAll()
      Table.GoogleTask -> googleTaskRepository.deleteAll()
      Table.RemoteFileMetadata -> remoteFileMetadataRepository.deleteAll()
      Table.EventOccurrence -> eventOccurrenceRepository.deleteAll()
      Table.EventHistory -> eventHistoryRepository.deleteAll()
    }
  }

  private suspend fun insertDemoReminders() {
    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)
    val groupUuId = reminderGroupRepository.defaultGroup()?.groupUuId ?: ""
    val reminders = listOf(
      Reminder(
        summary = "Team standup meeting",
        eventTime = dateTimeManager.getGmtFromDateTime(LocalDateTime.of(today, LocalTime.of(9, 0))),
        groupUuId = groupUuId,
      ),
      Reminder(
        summary = "Weekly grocery shopping",
        eventTime = dateTimeManager.getGmtFromDateTime(LocalDateTime.of(today, LocalTime.of(18, 30))),
        shoppings = listOf(
          ShopItem(summary = "Milk", createTime = "", isChecked = false),
          ShopItem(summary = "Fresh vegetables", createTime = "", isChecked = false),
          ShopItem(summary = "Coffee beans", createTime = "", isChecked = false),
          ShopItem(summary = "Birthday candles", createTime = "", isChecked = true),
        ),
        groupUuId = groupUuId,
      ),
      Reminder(
        summary = "Call Mom",
        eventTime = dateTimeManager.getGmtFromDateTime(LocalDateTime.of(today, LocalTime.of(20, 0))),
        type = 10 + Reminder.Action.CALL,
        target = "+1234567890",
        groupUuId = groupUuId,
      ),
      Reminder(
        summary = "Doctor's appointment",
        eventTime = dateTimeManager.getGmtFromDateTime(LocalDateTime.of(tomorrow, LocalTime.of(10, 30))),
        groupUuId = groupUuId,
      ),
      Reminder(
        summary = "Submit quarterly report",
        eventTime = dateTimeManager.getGmtFromDateTime(LocalDateTime.of(tomorrow, LocalTime.of(14, 0))),
        groupUuId = groupUuId,
      ),
      Reminder(
        summary = "Flight check-in",
        eventTime = dateTimeManager.getGmtFromDateTime(LocalDateTime.of(tomorrow, LocalTime.of(7, 0))),
        type = 10 + Reminder.Action.LINK,
        target = "https://www.google.com/travel/flights",
        groupUuId = groupUuId,
      ),
    )
    reminders.forEach { reminderRepository.save(it) }
  }

  private suspend fun insertDemoBirthdays() {
    val today = LocalDate.now()
    listOf(
      Triple("Mom", 0L, 1962),
      Triple("Alex Johnson", 1L, 1990),
      Triple("Sophia (Best Friend)", 3L, 1993),
      Triple("Dad", 6L, 1958),
    ).forEach { (name, daysAhead, birthYear) ->
      val upcoming = today.plusDays(daysAhead)
      val date = LocalDate.of(birthYear, upcoming.monthValue, upcoming.dayOfMonth)
      birthdayRepository.save(
        Birthday(
          name = name,
          date = dateTimeManager.formatBirthdayDate(date),
          day = date.dayOfMonth,
          month = date.monthValue - 1,
          dayMonth = "${date.dayOfMonth}|${date.monthValue - 1}",
          syncState = SyncState.Synced,
        ),
      )
    }
  }

  private suspend fun insertDemoNotes() {
    val notes = listOf(
      DemoNote(
        title = "Grocery List",
        summary = "Milk, eggs, bread, fresh basil, olive oil, and don't forget the candles for Saturday!",
        color = ThemeProvider.Color.GREEN,
      ),
      DemoNote(
        title = "Weekend Trip Ideas",
        summary =
          "1. Hike the coastal trail\n2. Visit the farmers market\n3. Try that new ramen place downtown\n4. Sunset photos at the pier",
        color = ThemeProvider.Color.LIGHT_BLUE,
      ),
      DemoNote(
        title = "Meeting Notes - Product Sync",
        summary =
          "Discussed Q3 roadmap. Action items: finalize onboarding flow, review pricing page copy, schedule user interviews for next sprint.",
        color = ThemeProvider.Color.AMBER,
      ),
      DemoNote(
        title = "Book Recommendations",
        summary = "- Atomic Habits\n- Project Hail Mary\n- The Midnight Library\n- Deep Work",
        color = ThemeProvider.Color.DEEP_PURPLE,
      ),
      DemoNote(
        title = "Favorite Quote",
        summary = "\"The secret of getting ahead is getting started.\" - Mark Twain",
        color = ThemeProvider.Color.PINK,
      ),
    )
    notes.forEach { demoNote ->
      noteRepository.save(
        Note(
          title = demoNote.title,
          summary = demoNote.summary,
          color = demoNote.color,
          date = dateTimeManager.getNowGmtDateTime(),
          syncState = SyncState.Synced,
        ),
      )
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
    private val TABLE_OPTIONS = Table.entries.map { it.tableName }
  }

  private data class DemoNote(
    val title: String,
    val summary: String,
    val color: Int,
  )
}
