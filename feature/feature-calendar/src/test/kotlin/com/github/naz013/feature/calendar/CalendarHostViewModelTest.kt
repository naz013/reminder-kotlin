package com.github.naz013.feature.calendar

import com.github.naz013.testing.BaseTest
import com.github.naz013.datecalc.DateTimeManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class CalendarHostViewModelTest : BaseTest() {
  private val dateTimeManager = mockk<DateTimeManager>()
  private val calendarPreferences = mockk<CalendarPreferences>(relaxed = true)

  private val today = LocalDate.of(2026, 7, 15)

  @Before
  override fun setUp() {
    super.setUp()
    every { dateTimeManager.fromMillis(any()) } returns LocalDateTime.of(today, LocalTime.NOON)
    every { calendarPreferences.lastViewMode } returns CalendarViewMode.MONTH
  }

  private fun viewModel(forcedMode: CalendarViewMode? = null) =
    CalendarHostViewModel(
      initialDateMillis = 1L,
      forcedMode = forcedMode,
      dateTimeManager = dateTimeManager,
      calendarPreferences = calendarPreferences,
    )

  @Test
  fun `initial mode comes from preferences when no mode is forced`() {
    every { calendarPreferences.lastViewMode } returns CalendarViewMode.SEVEN_DAY

    assertEquals(CalendarViewMode.SEVEN_DAY, viewModel().mode.value)
  }

  @Test
  fun `forced mode overrides the persisted preference`() {
    every { calendarPreferences.lastViewMode } returns CalendarViewMode.MONTH

    assertEquals(CalendarViewMode.DAY, viewModel(forcedMode = CalendarViewMode.DAY).mode.value)
  }

  @Test
  fun `anchor date is derived from the initial millis`() {
    assertEquals(today, viewModel().anchorDate.value)
  }

  @Test
  fun `onModeSelected updates the mode and persists it`() {
    val vm = viewModel()

    vm.onModeSelected(CalendarViewMode.THREE_DAY)

    assertEquals(CalendarViewMode.THREE_DAY, vm.mode.value)
    verify { calendarPreferences.lastViewMode = CalendarViewMode.THREE_DAY }
  }

  @Test
  fun `onModeSelected resets the anchor to today when switching to a day-based mode`() {
    val vm = viewModel(forcedMode = CalendarViewMode.MONTH)
    vm.onAnchorDateChanged(LocalDate.of(2020, 1, 1))

    vm.onModeSelected(CalendarViewMode.SEVEN_DAY)

    assertEquals(LocalDate.now(), vm.anchorDate.value)
  }

  @Test
  fun `onModeSelected preserves the anchor when switching to Month`() {
    val vm = viewModel(forcedMode = CalendarViewMode.DAY)
    val customAnchor = LocalDate.of(2026, 5, 1)
    vm.onAnchorDateChanged(customAnchor)

    vm.onModeSelected(CalendarViewMode.MONTH)

    assertEquals(customAnchor, vm.anchorDate.value)
  }

  @Test
  fun `onModeSelected is a no-op when the mode is unchanged`() {
    every { calendarPreferences.lastViewMode } returns CalendarViewMode.MONTH
    val vm = viewModel()

    vm.onModeSelected(CalendarViewMode.MONTH)

    verify(exactly = 0) { calendarPreferences.lastViewMode = any() }
  }

  @Test
  fun `openDay switches to Day mode anchored on the given date and persists Day`() {
    val vm = viewModel()
    val target = LocalDate.of(2026, 8, 3)

    vm.openDay(target)

    assertEquals(CalendarViewMode.DAY, vm.mode.value)
    assertEquals(target, vm.anchorDate.value)
    verify { calendarPreferences.lastViewMode = CalendarViewMode.DAY }
  }

  @Test
  fun `openDay from millis converts through the date-time manager`() {
    val target = LocalDate.of(2026, 8, 3)
    every { dateTimeManager.fromMillis(999L) } returns LocalDateTime.of(target, LocalTime.NOON)
    val vm = viewModel()

    vm.openDay(999L)

    assertEquals(CalendarViewMode.DAY, vm.mode.value)
    assertEquals(target, vm.anchorDate.value)
  }

  @Test
  fun `onAnchorDateChanged updates the anchor without touching the mode`() {
    val vm = viewModel(forcedMode = CalendarViewMode.SEVEN_DAY)
    val target = LocalDate.of(2026, 9, 1)

    vm.onAnchorDateChanged(target)

    assertEquals(target, vm.anchorDate.value)
    assertEquals(CalendarViewMode.SEVEN_DAY, vm.mode.value)
  }
}
