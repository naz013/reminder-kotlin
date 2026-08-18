package com.github.naz013.feature.calendar.timeline

import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import com.github.naz013.ui.agenda.AgendaCategory
import com.github.naz013.ui.agenda.UiAgendaBirthday
import com.github.naz013.ui.agenda.UiAgendaReminder
import com.github.naz013.ui.common.text.UiTextElement
import com.github.naz013.ui.common.text.UiTextFormat
import com.github.naz013.ui.reminder.UiReminderListActions
import com.github.naz013.ui.reminder.UiReminderListState
import com.github.naz013.feature.calendar.CalendarPreferences
import com.github.naz013.datecalc.DateTimeManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.temporal.ChronoUnit

class TimelineViewModelTest : BaseTest() {
  private val dateTimeManager = mockk<DateTimeManager>()
  private val calendarPreferences = mockk<CalendarPreferences>()
  private val getRangeEventItemsUseCase = mockk<GetRangeEventItemsUseCase>()
  private val getRangeHolidaysUseCase = mockk<GetRangeHolidaysUseCase>()

  private val startDate = LocalDate.of(2026, 7, 15)

  @Before
  override fun setUp() {
    super.setUp()
    every { dateTimeManager.fromMillis(any()) } returns LocalDateTime.of(startDate, LocalTime.NOON)
    every { dateTimeManager.getTime(any()) } returns "00:00"
    every { dateTimeManager.formatCalendarDay(any()) } returns "15"
    every { dateTimeManager.formatMonth(any()) } returns "July"
    every { dateTimeManager.formatCalendarWeekday(any()) } returns "Wed"
    every { calendarPreferences.startDay } returns 1
  }

  private fun viewModel(daySpan: Int) =
    TimelineViewModel(
      startDateMillis = 1L,
      daySpan = daySpan,
      dispatcherProvider = mockDispatcherProvider(),
      dateTimeManager = dateTimeManager,
      calendarPreferences = calendarPreferences,
      getRangeEventItemsUseCase = getRangeEventItemsUseCase,
      getRangeHolidaysUseCase = getRangeHolidaysUseCase,
    )

  private fun reminderItem(id: String = "r1") =
    UiAgendaReminder(
      id = id,
      dateTime = LocalDateTime.of(2026, 7, 15, 9, 0),
      category = AgendaCategory.REMINDERS,
      mainText = UiTextElement(id, UiTextFormat(fontSize = 14f)),
      secondaryText = null,
      tertiaryText = null,
      tags = emptyList(),
      actions = UiReminderListActions(),
      state = UiReminderListState(),
    )

  private fun birthdayItem(id: String = "b1") =
    UiAgendaBirthday(
      id = id,
      dateTime = LocalDateTime.of(2026, 7, 15, 0, 0),
      name = "Alice",
      ageFormatted = "25",
      remainingTimeFormatted = null,
      color = 0,
      contrastColor = 0,
      dateFormatted = "15 Jul",
    )

  @Test
  fun `builds 24 hour labels on creation`() {
    assertEquals(24, viewModel(daySpan = 3).state.value.hourLabels.size)
  }

  @Test
  fun `applies a range title on creation for multi-day spans`() {
    assertEquals("15 – 15 July", viewModel(daySpan = 3).state.value.title)
  }

  @Test
  fun `applies a single-date title for the 1-day span`() {
    assertEquals("15 July", viewModel(daySpan = 1).state.value.title)
  }

  @Test
  fun `3-day initial window is centered on the anchor date`() {
    val vm = viewModel(daySpan = 3)
    val center = vm.positionForDate(startDate)
    val windowStart = vm.windowStartForPosition(center)

    assertEquals(startDate.minusDays(1), windowStart)
    assertEquals(
      listOf(startDate.minusDays(1), startDate, startDate.plusDays(1)),
      vm.daysForWindow(windowStart).map { it.date },
    )
  }

  @Test
  fun `3-day window slides by three days per position`() {
    val vm = viewModel(daySpan = 3)
    val center = vm.positionForDate(startDate)
    val base = vm.windowStartForPosition(center)

    assertEquals(base.plusDays(3), vm.windowStartForPosition(center + 1))
    assertEquals(base.minusDays(3), vm.windowStartForPosition(center - 1))
  }

  @Test
  fun `3-day positionForDate maps a date to the fixed block containing it`() {
    val vm = viewModel(daySpan = 3)
    val date = startDate.plusDays(6)

    val windowStart = vm.windowStartForPosition(vm.positionForDate(date))

    assertEquals(startDate.plusDays(5), windowStart)
    assertEquals(true, vm.daysForWindow(windowStart).any { it.date == date })
  }

  @Test
  fun `3-day window exposes exactly three days`() {
    val vm = viewModel(daySpan = 3)
    val days = vm.daysForWindow(startDate)

    assertEquals(3, days.size)
    assertEquals(listOf(startDate, startDate.plusDays(1), startDate.plusDays(2)), days.map { it.date })
  }

  @Test
  fun `middayForPosition returns the centered anchor for 3-day windows`() {
    val vm = viewModel(daySpan = 3)
    val center = vm.positionForDate(startDate)

    assertEquals(startDate, vm.middayForPosition(center))
  }

  @Test
  fun `middayForPosition returns the middle day of a 7-day calendar week`() {
    val vm = viewModel(daySpan = 7)
    val center = vm.positionForDate(startDate)
    val base = vm.windowStartForPosition(center)

    assertEquals(base.plusDays(3), vm.middayForPosition(center))
  }

  @Test
  fun `7-day window steps by a whole week and spans seven days`() {
    val vm = viewModel(daySpan = 7)
    val center = vm.positionForDate(startDate)
    val base = vm.windowStartForPosition(center)

    assertEquals(7L, ChronoUnit.DAYS.between(base, vm.windowStartForPosition(center + 1)))
    assertEquals(7, vm.daysForWindow(base).size)
  }

  @Test
  fun `7-day window is aligned to the start of the week`() {
    val vm = viewModel(daySpan = 7)
    val center = vm.positionForDate(startDate)
    val base = vm.windowStartForPosition(center)

    // The window containing startDate must start on or before it and cover it.
    assertEquals(true, !base.isAfter(startDate))
    assertEquals(true, base.plusDays(6) >= startDate)
  }

  @Test
  fun `loadWindowEvents queries the whole span`() =
    runTest {
      val vm = viewModel(daySpan = 3)
      val expected = mapOf(startDate to listOf(reminderItem("r1")))
      coEvery { getRangeEventItemsUseCase(startDate, startDate.plusDays(2)) } returns expected

      val result = vm.loadWindowEvents(startDate)

      assertEquals(expected, result)
    }

  @Test
  fun `loadWindowHolidays queries the whole span`() =
    runTest {
      val vm = viewModel(daySpan = 7)
      coEvery { getRangeHolidaysUseCase(startDate, startDate.plusDays(6)) } returns emptyMap()

      val result = vm.loadWindowHolidays(startDate)

      assertEquals(emptyMap<LocalDate, com.github.naz013.domain.PublicHoliday>(), result)
    }

  @Test
  fun `onItemClick on a reminder posts OpenReminderPreview`() {
    val vm = viewModel(daySpan = 3)

    vm.onItemClick(reminderItem("r1"))

    assertEquals(
      TimelineViewModel.NavigationEvent.OpenReminderPreview("r1"),
      vm.navigationEvent.value?.peekContent(),
    )
  }

  @Test
  fun `onItemClick on a birthday posts OpenBirthdayPreview`() {
    val vm = viewModel(daySpan = 3)

    vm.onItemClick(birthdayItem("b1"))

    assertEquals(
      TimelineViewModel.NavigationEvent.OpenBirthdayPreview("b1"),
      vm.navigationEvent.value?.peekContent(),
    )
  }

  @Test
  fun `onAddReminderClick posts OpenNewReminder with the millis for the given date`() {
    every { dateTimeManager.toMillis(any<LocalDateTime>()) } returns 42L
    val vm = viewModel(daySpan = 3)

    vm.onAddReminderClick(startDate)

    assertEquals(
      TimelineViewModel.NavigationEvent.OpenNewReminder(42L),
      vm.navigationEvent.value?.peekContent(),
    )
  }

  @Test
  fun `onAddBirthdayClick posts OpenNewBirthday for the given date`() {
    val vm = viewModel(daySpan = 3)

    vm.onAddBirthdayClick(startDate)

    assertEquals(
      TimelineViewModel.NavigationEvent.OpenNewBirthday(startDate),
      vm.navigationEvent.value?.peekContent(),
    )
  }
}
