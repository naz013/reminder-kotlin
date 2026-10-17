package com.elementary.tasks.core.home

import android.content.Context
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.feature.home.HomePreferences
import com.github.naz013.feature.home.scheduleview.GetActiveEventsForTheDayUseCase
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderSettingsRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.testing.BaseTest
import com.github.naz013.ui.agenda.UiAgendaItemAdapter
import com.github.naz013.ui.agenda.UiAgendaReminder
import com.github.naz013.ui.birthday.UiBirthdayListAdapter
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter
import com.github.naz013.ui.common.text.UiTextElement
import com.github.naz013.ui.common.text.UiTextFormat
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.reminder.ReminderRowMapper
import com.github.naz013.ui.reminder.UiReminderList
import com.github.naz013.ui.reminder.UiReminderListActions
import com.github.naz013.ui.reminder.UiReminderListAdapter
import com.github.naz013.ui.reminder.UiReminderListState
import com.github.naz013.domain.reminder.v2.NotificationSettings
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class ReminderRowEquivalenceTest : BaseTest() {
  private val now = LocalDateTime.of(2026, 7, 24, 12, 0)
  private val dateTimeManager = mockk<DateTimeManager>(relaxed = true)
  private val reminderRepository = mockk<ReminderV2Repository>()
  private val uiReminderListAdapter = mockk<UiReminderListAdapter>()

  private lateinit var homeUseCase: GetActiveEventsForTheDayUseCase
  private lateinit var agendaAdapter: UiAgendaItemAdapter

  @Before
  override fun setUp() {
    super.setUp()
    mockkObject(ThemeProvider.Companion)
    every { ThemeProvider.themedColor(any(), any()) } returns 0
    every { dateTimeManager.getCurrentDateTime() } returns now
    every { dateTimeManager.utcToLocal(any()) } answers { firstArg() }
    every { dateTimeManager.getHeaderDateFormatted(any()) } returns "header"
    coEvery { reminderRepository.observeActiveInRange(any(), any(), any()) } returns flowOf(emptyList())

    val groupRepository = mockk<GroupV2Repository>()
    coEvery { groupRepository.getAll() } returns emptyList()
    val contextProvider = mockk<ContextProvider>()
    every { contextProvider.themedContext } returns mockk<Context>(relaxed = true)
    every { contextProvider.context } returns mockk<Context>(relaxed = true)
    val settingsRepository = mockk<ReminderSettingsRepository>()
    every { settingsRepository.getNotificationDefaults() } returns NotificationSettings()
    val mapper = ReminderRowMapper(dateTimeManager, mockk(relaxed = true))

    homeUseCase = GetActiveEventsForTheDayUseCase(
      dateTimeManager = dateTimeManager,
      reminderV2Repository = reminderRepository,
      birthdayRepository = mockk<BirthdayRepository>(relaxed = true).also {
        every { it.observeAll(any()) } returns flowOf(emptyList())
      },
      groupV2Repository = groupRepository,
      modelDateTimeFormatter = mockk<ModelDateTimeFormatter>(relaxed = true),
      textProvider = mockk<TextProvider>(relaxed = true),
      reminderRowMapper = mapper,
      contextProvider = contextProvider,
      homePreferences = mockk<HomePreferences>(relaxed = true),
    )
    agendaAdapter = UiAgendaItemAdapter(
      uiReminderListAdapter,
      mockk<UiBirthdayListAdapter>(),
      dateTimeManager,
      mockk<TextProvider>(relaxed = true),
      settingsRepository,
      mapper,
    )
  }

  @After
  override fun tearDown() {
    super.tearDown()
    unmockkObject(ThemeProvider.Companion)
  }

  @Test
  fun `home and agenda agree on due time and overdue flag for the same reminder`() = runTest {
    listOf(now.minusHours(2), now, now.plusHours(2)).forEach { due ->
      val reminder = ReminderV2(
        uuId = "r",
        summary = "Call",
        schedule = ReminderSchedule(startDateTime = now, eventDateTime = due),
        action = ReminderAction.Call(target = "123"),
      )
      every { reminderRepository.observeActiveInRange(any(), any(), any()) } returns flowOf(listOf(reminder))
      every { uiReminderListAdapter.createV2(reminder, null) } returns uiReminderList(due)

      val home = homeUseCase(now).first().single()
      val agenda = agendaAdapter.convertV2(listOf(reminder), emptyMap(), emptyList())
        .filterIsInstance<UiAgendaReminder>().single()

      assertEquals(home.isOverdue, agenda.isOverdue)
      assertEquals(home.date, agenda.dateTime.toLocalDate())
      assertEquals(home.time, agenda.dateTime.toLocalTime())
      assertEquals("123", home.description)
    }
  }

  private fun uiReminderList(due: LocalDateTime) = UiReminderList(
    id = "r",
    noteId = null,
    dueDateTime = due,
    mainText = UiTextElement(text = "Call", textFormat = UiTextFormat(fontSize = 14f)),
    secondaryText = null,
    tertiaryText = null,
    tags = emptyList(),
    actions = UiReminderListActions(canToggle = true, canOpen = true, canEdit = true, canDelete = true, canSkip = false),
    state = UiReminderListState(isActive = true, isRemoved = false, isGps = false, isPinned = false),
  )
}
