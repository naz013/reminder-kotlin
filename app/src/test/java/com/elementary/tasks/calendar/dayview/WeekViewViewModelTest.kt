package com.elementary.tasks.calendar.dayview

import com.elementary.tasks.BaseTest
import com.elementary.tasks.birthdays.usecase.DeleteBirthdayUseCase
import com.elementary.tasks.calendar.dayview.weekheader.WeekHeaderController
import com.elementary.tasks.core.data.ui.UiTextElement
import com.elementary.tasks.core.text.UiTextFormat
import com.elementary.tasks.home.agenda.AgendaCategory
import com.elementary.tasks.home.agenda.AgendaMenuAction
import com.elementary.tasks.home.agenda.UiAgendaBirthday
import com.elementary.tasks.home.agenda.UiAgendaReminder
import com.elementary.tasks.mockDispatcherProvider
import com.elementary.tasks.reminder.lists.data.UiReminderListActions
import com.elementary.tasks.reminder.lists.data.UiReminderListState
import com.elementary.tasks.reminder.scheduling.usecase.ToggleReminderStateUseCase
import com.elementary.tasks.reminder.usecase.MoveReminderToArchiveUseCase
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.ReminderV2Repository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalDateTime.of

class WeekViewViewModelTest : BaseTest() {
  private val weekHeaderController = mockk<WeekHeaderController>()
  private val dateTimeManager = mockk<DateTimeManager>()
  private val getDayEventItemsUseCase = mockk<GetDayEventItemsUseCase>()
  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private val moveReminderToArchiveUseCase = mockk<MoveReminderToArchiveUseCase>(relaxed = true)
  private val toggleReminderStateUseCase = mockk<ToggleReminderStateUseCase>(relaxed = true)
  private val deleteBirthdayUseCase = mockk<DeleteBirthdayUseCase>(relaxed = true)

  private lateinit var viewModel: WeekViewViewModel

  private val startDate = LocalDate.of(2026, 7, 15)

  @Before
  override fun setUp() {
    super.setUp()
    every { dateTimeManager.fromMillis(any()) } returns LocalDateTime.of(startDate, org.threeten.bp.LocalTime.NOON)
    every { dateTimeManager.formatCalendarDate(any()) } returns "wednesday, 15 july"
    every { dateTimeManager.toMillis(any<LocalDateTime>()) } returns 1L
    coEvery { weekHeaderController.calculateWeek(any()) } returns emptyList()

    viewModel =
      WeekViewViewModel(
        startDateMillis = 1L,
        dispatcherProvider = mockDispatcherProvider(),
        weekHeaderController = weekHeaderController,
        dateTimeManager = dateTimeManager,
        getDayEventItemsUseCase = getDayEventItemsUseCase,
        reminderV2Repository = reminderV2Repository,
        moveReminderToArchiveUseCase = moveReminderToArchiveUseCase,
        toggleReminderStateUseCase = toggleReminderStateUseCase,
        deleteBirthdayUseCase = deleteBirthdayUseCase,
      )
  }

  private fun reminderItem(
    id: String = "r1",
    isGps: Boolean = false,
  ) = UiAgendaReminder(
    id = id,
    dateTime = of(2026, 7, 15, 9, 0),
    category = AgendaCategory.REMINDERS,
    mainText = UiTextElement(id, UiTextFormat(fontSize = 14f)),
    secondaryText = null,
    tertiaryText = null,
    tags = emptyList(),
    actions = UiReminderListActions(),
    state = UiReminderListState(isGps = isGps),
  )

  private fun birthdayItem(id: String = "b1") =
    UiAgendaBirthday(
      id = id,
      dateTime = of(2026, 7, 15, 0, 0),
      name = "Alice",
      ageFormatted = "25",
      remainingTimeFormatted = null,
      color = 0,
      contrastColor = 0,
      dateFormatted = "15 Jul",
    )

  @Test
  fun `initializes title and days for the start date on creation`() {
    assertEquals("Wednesday, 15 july", viewModel.state.value.title)
    assertEquals(startDate, viewModel.state.value.selectedDate)
    assertEquals(1, viewModel.refreshSignal.value)
  }

  @Test
  fun `dateForPosition offsets from the center position by whole days`() {
    val center = viewModel.positionForDate(viewModel.initDate)

    assertEquals(viewModel.initDate.plusDays(3), viewModel.dateForPosition(center + 3))
    assertEquals(viewModel.initDate.minusDays(2), viewModel.dateForPosition(center - 2))
  }

  @Test
  fun `positionForDate is the inverse of dateForPosition`() {
    val date = viewModel.initDate.plusDays(5)

    val position = viewModel.positionForDate(date)

    assertEquals(date, viewModel.dateForPosition(position))
  }

  @Test
  fun `updateLastPosition stores the given position`() {
    viewModel.updateLastPosition(42)

    assertEquals(42, viewModel.lastPosition)
  }

  @Test
  fun `selectDate recomputes state and posts MoveToDate`() {
    val date = LocalDate.of(2026, 7, 20)
    every { dateTimeManager.formatCalendarDate(date) } returns "monday, 20 july"

    viewModel.selectDate(date)

    assertEquals("Monday, 20 july", viewModel.state.value.title)
    assertEquals(date, viewModel.state.value.selectedDate)
    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(WeekViewViewModel.NavigationEvent.MoveToDate(date), event)
  }

  @Test
  fun `onDateSelected recomputes state without posting a navigation event`() {
    val date = LocalDate.of(2026, 7, 20)
    every { dateTimeManager.formatCalendarDate(date) } returns "monday, 20 july"

    viewModel.onDateSelected(date)

    assertEquals(date, viewModel.state.value.selectedDate)
    assertEquals(null, viewModel.navigationEvent.value)
  }

  @Test
  fun `loadDayEvents delegates to the use case`() =
    runTest {
      val date = LocalDate.of(2026, 7, 15)
      val items = listOf(reminderItem("r1"))
      coEvery { getDayEventItemsUseCase(date) } returns items

      val result = viewModel.loadDayEvents(date)

      assertEquals(items, result)
    }

  @Test
  fun `onItemClick on a reminder posts OpenReminderPreview`() {
    viewModel.onItemClick(reminderItem("r1"))

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(WeekViewViewModel.NavigationEvent.OpenReminderPreview("r1"), event)
  }

  @Test
  fun `onItemClick on a birthday posts OpenBirthdayPreview`() {
    viewModel.onItemClick(birthdayItem("b1"))

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(WeekViewViewModel.NavigationEvent.OpenBirthdayPreview("b1"), event)
  }

  @Test
  fun `onAgendaMenuAction OPEN on a reminder posts OpenReminderPreview`() {
    viewModel.onAgendaMenuAction(reminderItem("r1"), AgendaMenuAction.OPEN)

    assertEquals(
      WeekViewViewModel.NavigationEvent.OpenReminderPreview("r1"),
      viewModel.navigationEvent.value?.peekContent(),
    )
  }

  @Test
  fun `onAgendaMenuAction EDIT on a reminder posts OpenReminderEdit`() {
    viewModel.onAgendaMenuAction(reminderItem("r1"), AgendaMenuAction.EDIT)

    assertEquals(
      WeekViewViewModel.NavigationEvent.OpenReminderEdit("r1"),
      viewModel.navigationEvent.value?.peekContent(),
    )
  }

  @Test
  fun `onAgendaMenuAction ARCHIVE on a reminder posts ConfirmArchiveReminder`() {
    viewModel.onAgendaMenuAction(reminderItem("r1"), AgendaMenuAction.ARCHIVE)

    assertEquals(
      WeekViewViewModel.NavigationEvent.ConfirmArchiveReminder("r1"),
      viewModel.navigationEvent.value?.peekContent(),
    )
  }

  @Test
  fun `onAgendaMenuAction TURN_OFF on a non-gps reminder toggles it directly`() {
    val reminder = ReminderV2(uuId = "r1", schedule = ReminderSchedule(startDateTime = LocalDateTime.now()))
    coEvery { reminderV2Repository.getById("r1") } returns reminder

    viewModel.onAgendaMenuAction(reminderItem("r1", isGps = false), AgendaMenuAction.TURN_OFF)

    coVerify(exactly = 1) { toggleReminderStateUseCase(reminder) }
    assertEquals(null, viewModel.navigationEvent.value)
  }

  @Test
  fun `onAgendaMenuAction TURN_OFF on a gps reminder requests location permission instead of toggling`() {
    viewModel.onAgendaMenuAction(reminderItem("r1", isGps = true), AgendaMenuAction.TURN_OFF)

    coVerify(exactly = 0) { toggleReminderStateUseCase(any()) }
    assertEquals(
      WeekViewViewModel.NavigationEvent.RequestGpsPermission("r1"),
      viewModel.navigationEvent.value?.peekContent(),
    )
  }

  @Test
  fun `onAgendaMenuAction DELETE on a reminder does nothing`() {
    viewModel.onAgendaMenuAction(reminderItem("r1"), AgendaMenuAction.DELETE)

    assertEquals(null, viewModel.navigationEvent.value)
  }

  @Test
  fun `onAgendaMenuAction EDIT on a birthday posts OpenBirthdayEdit`() {
    viewModel.onAgendaMenuAction(birthdayItem("b1"), AgendaMenuAction.EDIT)

    assertEquals(
      WeekViewViewModel.NavigationEvent.OpenBirthdayEdit("b1"),
      viewModel.navigationEvent.value?.peekContent(),
    )
  }

  @Test
  fun `onAgendaMenuAction DELETE on a birthday posts ConfirmDeleteBirthday`() {
    viewModel.onAgendaMenuAction(birthdayItem("b1"), AgendaMenuAction.DELETE)

    assertEquals(
      WeekViewViewModel.NavigationEvent.ConfirmDeleteBirthday("b1"),
      viewModel.navigationEvent.value?.peekContent(),
    )
  }

  @Test
  fun `onAgendaMenuAction ARCHIVE on a birthday does nothing`() {
    viewModel.onAgendaMenuAction(birthdayItem("b1"), AgendaMenuAction.ARCHIVE)

    assertEquals(null, viewModel.navigationEvent.value)
  }

  @Test
  fun `toggleReminder toggles the reminder state and bumps the refresh signal`() {
    val reminder = ReminderV2(uuId = "r1", schedule = ReminderSchedule(startDateTime = LocalDateTime.now()))
    coEvery { reminderV2Repository.getById("r1") } returns reminder

    viewModel.toggleReminder("r1")

    coVerify(exactly = 1) { toggleReminderStateUseCase(reminder) }
    assertEquals(2, viewModel.refreshSignal.value)
  }

  @Test
  fun `toggleReminder does nothing when the reminder is not found`() {
    coEvery { reminderV2Repository.getById("missing") } returns null

    viewModel.toggleReminder("missing")

    coVerify(exactly = 0) { toggleReminderStateUseCase(any()) }
    assertEquals(2, viewModel.refreshSignal.value)
  }

  @Test
  fun `moveReminderToArchive archives by id and bumps the refresh signal`() {
    viewModel.moveReminderToArchive("r1")

    coVerify(exactly = 1) { moveReminderToArchiveUseCase("r1") }
    assertEquals(2, viewModel.refreshSignal.value)
  }

  @Test
  fun `deleteBirthday deletes by id and bumps the refresh signal`() {
    viewModel.deleteBirthday("b1")

    coVerify(exactly = 1) { deleteBirthdayUseCase("b1") }
    assertEquals(2, viewModel.refreshSignal.value)
  }

  @Test
  fun `onAddReminderClick posts OpenNewReminder with the millis for the given date`() {
    val date = LocalDate.of(2026, 7, 20)

    viewModel.onAddReminderClick(date)

    assertEquals(
      WeekViewViewModel.NavigationEvent.OpenNewReminder(1L),
      viewModel.navigationEvent.value?.peekContent(),
    )
  }

  @Test
  fun `onAddBirthdayClick posts OpenNewBirthday for the given date`() {
    val date = LocalDate.of(2026, 7, 20)

    viewModel.onAddBirthdayClick(date)

    assertEquals(
      WeekViewViewModel.NavigationEvent.OpenNewBirthday(date),
      viewModel.navigationEvent.value?.peekContent(),
    )
  }
}
