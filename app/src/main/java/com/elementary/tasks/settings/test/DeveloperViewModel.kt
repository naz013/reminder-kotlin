package com.elementary.tasks.settings.test

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.ShopItemV2
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
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.PlaceRepository
import com.github.naz013.repository.RecentQueryRepository
import com.github.naz013.repository.RecurPresetRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.RemoteFileMetadataRepository
import com.github.naz013.repository.UsedTimeRepository
import com.github.naz013.repository.WorkflowRuleRepository
import com.github.naz013.repository.WorkflowTemplateRepository
import com.github.naz013.repository.table.Table
import com.github.naz013.reviews.AppSource
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
  private val remoteFileMetadataRepository: RemoteFileMetadataRepository,
  private val usedTimeRepository: UsedTimeRepository,
  private val buildInfo: BuildInfo,
  private val reminderV2Repository: ReminderV2Repository,
  private val groupV2Repository: GroupV2Repository,
  private val workflowRuleRepository: WorkflowRuleRepository,
  private val workflowTemplateRepository: WorkflowTemplateRepository,
) : ViewModel() {
  val state: StateFlow<DeveloperState> field = MutableStateFlow(DeveloperState())
  val navigationEvent: LiveData<Event<DeveloperEvent>> field = mutableLiveEventOf()

  fun onResetBannersClick() {
    legalDocumentRepository.resetSeen(LegalDocumentType.PRIVACY_POLICY)
    prefs.isUserLogged = false
    prefs.lastVersionCode = 0
    navigationEvent.value = Event(DeveloperEvent.BannersReset)
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
      navigationEvent.postValue(Event(DeveloperEvent.ShowMessage("All tables have been cleared")))
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
      navigationEvent.postValue(Event(DeveloperEvent.ShowMessage("Demo data has been inserted")))
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
    navigationEvent.value = Event(
      DeveloperEvent.OpenReviewDialog(
        appSource = if (buildInfo.isPro) AppSource.PRO else AppSource.FREE
      )
    )
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
      reminderV2Repository.save(reminder)
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
      navigationEvent.postValue(Event(DeveloperEvent.ShowMessage("${table.tableName} table has been cleared")))
    }
  }

  private suspend fun clearTable(table: Table) {
    when (table) {
      Table.Birthday -> birthdayRepository.deleteAll()
      Table.RecentQuery -> recentQueryRepository.deleteAll()
      Table.RecurPreset -> recurPresetRepository.deleteAll()
      Table.UsedTime -> usedTimeRepository.deleteAll()
      Table.CalendarEvent -> calendarEventRepository.deleteAll()
      Table.ReminderGroup -> { }
      Table.Reminder -> { }
      Table.Place -> placeRepository.deleteAll()
      Table.Note -> noteRepository.deleteAllNotes()
      Table.ImageFile -> noteRepository.deleteAllImages()
      Table.GoogleTaskList -> googleTaskListRepository.deleteAll()
      Table.GoogleTask -> googleTaskRepository.deleteAll()
      Table.RemoteFileMetadata -> remoteFileMetadataRepository.deleteAll()
      Table.EventOccurrence -> eventOccurrenceRepository.deleteAll()
      Table.EventHistory -> eventHistoryRepository.deleteAll()
      Table.ReminderV2 -> reminderV2Repository.deleteAll()
      Table.GroupV2 -> groupV2Repository.deleteAll()
      Table.WorkflowRule -> workflowRuleRepository.deleteAll()
      Table.WorkflowTemplate -> workflowTemplateRepository.deleteAll()
    }
  }

  private suspend fun insertDemoReminders() {
    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)
    val groupId = groupV2Repository.defaultGroup()?.uuId
    val startDateTime = dateTimeManager.getCurrentDateTime()
    fun schedule(dateTime: LocalDateTime) =
      ReminderSchedule(startDateTime = startDateTime, eventDateTime = dateTimeManager.localToUtc(dateTime))
    val reminders =
      listOf(
        ReminderV2(
          summary = "Team standup meeting",
          schedule = schedule(LocalDateTime.of(today, LocalTime.of(9, 0))),
          groupId = groupId,
        ),
        ReminderV2(
          summary = "Weekly grocery shopping",
          schedule = schedule(LocalDateTime.of(today, LocalTime.of(18, 30))),
          action = ReminderAction.Shopping,
          shoppingItems =
            listOf(
              ShopItemV2(summary = "Milk", isChecked = false, createdAt = startDateTime),
              ShopItemV2(summary = "Fresh vegetables", isChecked = false, createdAt = startDateTime),
              ShopItemV2(summary = "Coffee beans", isChecked = false, createdAt = startDateTime),
              ShopItemV2(summary = "Birthday candles", isChecked = true, createdAt = startDateTime),
            ),
          groupId = groupId,
        ),
        ReminderV2(
          summary = "Call Mom",
          schedule = schedule(LocalDateTime.of(today, LocalTime.of(20, 0))),
          action = ReminderAction.Call(target = "+1234567890"),
          groupId = groupId,
        ),
        ReminderV2(
          summary = "Doctor's appointment",
          schedule = schedule(LocalDateTime.of(tomorrow, LocalTime.of(10, 30))),
          groupId = groupId,
        ),
        ReminderV2(
          summary = "Submit quarterly report",
          schedule = schedule(LocalDateTime.of(tomorrow, LocalTime.of(14, 0))),
          groupId = groupId,
        ),
        ReminderV2(
          summary = "Flight check-in",
          schedule = schedule(LocalDateTime.of(tomorrow, LocalTime.of(7, 0))),
          action = ReminderAction.Link(target = "https://www.google.com/travel/flights"),
          groupId = groupId,
        ),
      )
    reminders.forEach { reminderV2Repository.save(it) }
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
    val notes =
      listOf(
        DemoNote(
          title = "Grocery List",
          summary = "Milk, eggs, bread, fresh basil, olive oil, and don't forget the candles for Saturday!",
          color = ThemeProvider.AppColorIndex.GREEN,
        ),
        DemoNote(
          title = "Weekend Trip Ideas",
          summary =
            "1. Hike the coastal trail\n2. Visit the farmers market\n3. Try that new ramen place downtown\n4. Sunset photos at the pier",
          color = ThemeProvider.AppColorIndex.LIGHT_BLUE,
        ),
        DemoNote(
          title = "Meeting Notes - Product Sync",
          summary =
            "Discussed Q3 roadmap. Action items: finalize onboarding flow, review pricing page copy, schedule user interviews for next sprint.",
          color = ThemeProvider.AppColorIndex.AMBER,
        ),
        DemoNote(
          title = "Book Recommendations",
          summary = "- Atomic Habits\n- Project Hail Mary\n- The Midnight Library\n- Deep Work",
          color = ThemeProvider.AppColorIndex.DEEP_PURPLE,
        ),
        DemoNote(
          title = "Favorite Quote",
          summary = "\"The secret of getting ahead is getting started.\" - Mark Twain",
          color = ThemeProvider.AppColorIndex.PINK,
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

  private fun prepareReminder(selectedItem: Int): ReminderV2 {
    val now = LocalDateTime.now()
    val schedule = ReminderSchedule(startDateTime = now, eventDateTime = dateTimeManager.localToUtc(now))
    return when (selectedItem) {
      0 -> ReminderV2(summary = "This is a simple reminder.", schedule = schedule)

      1 ->
        ReminderV2(
          summary = "This is a recurring daily reminder.",
          schedule = schedule,
          recurrence = RecurrenceRule.Daily(repeatInterval = 24 * 60 * 60 * 1000L),
        )

      2 ->
        ReminderV2(
          summary = "This is a reminder with todo.",
          schedule = schedule,
          action = ReminderAction.Shopping,
          shoppingItems =
            listOf(
              ShopItemV2(summary = "Milk", isChecked = false, createdAt = now),
              ShopItemV2(summary = "Bread", isChecked = false, createdAt = now),
              ShopItemV2(summary = "Eggs", isChecked = false, createdAt = now),
              ShopItemV2(summary = "Butter", isChecked = true, createdAt = now),
            ),
        )

      3 ->
        ReminderV2(
          summary = "This is a reminder with call action.",
          schedule = schedule,
          action = ReminderAction.Call(target = "+1234567890"),
        )

      4 ->
        ReminderV2(
          summary = "This is a reminder with SMS action.",
          schedule = schedule,
          action = ReminderAction.Sms(target = "+1234567890", subject = ""),
        )

      5 ->
        ReminderV2(
          summary = "This is a test email from Tasks app.",
          schedule = schedule,
          action = ReminderAction.Email(target = "some@mail.com", subject = "Test Subject"),
        )

      6 ->
        ReminderV2(
          summary = "This is a reminder with open link action.",
          schedule = schedule,
          action = ReminderAction.Link(target = "https://www.google.com"),
        )

      7 ->
        ReminderV2(
          summary = "This is a reminder with open Chrome action.",
          schedule = schedule,
          action = ReminderAction.App(target = "com.android.chrome"),
        )

      else -> ReminderV2(schedule = schedule)
    }
  }

  private fun prepareBirthday(selectedItem: Int): Birthday =
    when (selectedItem) {
      0 -> Birthday(name = "John Doe", date = "1990-05-15", number = "", syncState = SyncState.Synced)
      1 -> Birthday(name = "Jane Smith", date = "1985-10-20", number = "+1234567890", syncState = SyncState.Synced)
      2 ->
        Birthday(
          name = "Alice Johnson",
          date = "2000-07-25",
          number = "",
          ignoreYear = true,
          syncState = SyncState.Synced,
        )

      else -> Birthday(syncState = SyncState.Synced)
    }

  companion object {
    private val REMINDER_OPTIONS =
      listOf(
        "Simple reminder",
        "Recurring reminder",
        "With Todo",
        "With Call action",
        "With SMS action",
        "With Email action",
        "With Open URL action",
        "With Open Chrome Browser action",
      )
    private val BIRTHDAY_OPTIONS =
      listOf(
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
