package com.github.naz013.feature.home.dayoverview

import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.feature.home.HomeEvent
import com.github.naz013.feature.home.scheduleview.ObserveTodayScheduleUseCase
import com.github.naz013.feature.home.scheduleview.TodayItemHomeEventMapper
import com.github.naz013.feature.home.scheduleview.TodaySnapshot
import com.github.naz013.logic.reminder.today.TodayBucket
import com.github.naz013.logic.reminder.today.TodayItem
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import com.github.naz013.ui.common.R
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import java.util.Locale

class DayOverviewViewModelTest : BaseTest() {
  private val observeTodayScheduleUseCase = mockk<ObserveTodayScheduleUseCase>()
  private val todayItemHomeEventMapper = mockk<TodayItemHomeEventMapper>()
  private val dateTimeManager = mockk<DateTimeManager>()
  private val textProvider = mockk<TextProvider>()

  private val today = LocalDate.of(2026, 10, 14)
  private val now = LocalDateTime.of(2026, 10, 14, 12, 0)

  @Before
  override fun setUp() {
    super.setUp()
    every { dateTimeManager.formatCalendarDate(any()) } returns "heading"
    every { dateTimeManager.getTime(any()) } answers { firstArg<LocalTime>().toString() }
    every { textProvider.getQuantityString(any(), any()) } answers {
      val label = when (firstArg<Int>()) {
        R.plurals.home_summary_left -> "left"
        R.plurals.home_summary_done -> "done"
        else -> "overdue"
      }
      "${secondArg<Int>()} $label"
    }
    every { textProvider.getString(R.string.day_overview_busy_now) } returns "busy"
    every { textProvider.getString(R.string.day_overview_free_rest_of_day) } returns "free all day"
    every { textProvider.getString(R.string.day_overview_free_until, *anyVararg()) } answers {
      "free until ${formatArgs(args)[0]}"
    }
    every { textProvider.getString(R.string.day_overview_largest_free, *anyVararg()) } answers {
      val formatArgs = formatArgs(args)
      "largest ${formatArgs[0]}-${formatArgs[1]}"
    }
    coEvery { todayItemHomeEventMapper(any(), any()) } returns listOf(homeEvent("Dentist"))
  }

  @Test
  fun `counts left done and overdue items in the summary`() = runTest {
    val state = stateFor(
      timed(10, 0),
      timed(15, 0),
      allDay(),
      overdue(),
    )

    assertEquals("2 left · 1 done · 1 overdue", state.summary)
  }

  @Test
  fun `picks the next timed item and uses the mapped title`() = runTest {
    val state = stateFor(timed(10, 0), timed(15, 0))

    assertEquals(DayOverviewState.NextItem(time = "15:00", title = "Dentist"), state.nextItem)
  }

  @Test
  fun `has no next item when only past and all day items exist`() = runTest {
    val state = stateFor(timed(10, 0), allDay())

    assertNull(state.nextItem)
  }

  @Test
  fun `reports free until the next item`() = runTest {
    val state = stateFor(timed(15, 0))

    assertEquals("free until 15:00", state.freeTime.current)
    assertEquals("largest 15:30-00:00", state.freeTime.largest)
  }

  @Test
  fun `reports busy while an item is in progress`() = runTest {
    val state = stateFor(timed(12, 0))

    assertEquals("busy", state.freeTime.current)
  }

  @Test
  fun `reports free for the rest of the day and labels the end of day as midnight`() = runTest {
    val state = stateFor(allDay())

    assertEquals("free all day", state.freeTime.current)
    assertEquals("largest 12:00-00:00", state.freeTime.largest)
  }

  @Test
  fun `starts the week on Monday for a Monday-start locale`() {
    val days = buildWeekDays(today, DayOfWeek.MONDAY, Locale.ENGLISH)

    assertEquals(listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"), days.map { it.weekDayLabel })
    assertEquals("12", days.first().dayOfMonth)
    assertEquals(listOf(false, false, true, false, false, false, false), days.map { it.isToday })
  }

  @Test
  fun `starts the week on Sunday for a Sunday-start locale`() {
    val days = buildWeekDays(today, DayOfWeek.SUNDAY, Locale.ENGLISH)

    assertEquals("Sun", days.first().weekDayLabel)
    assertEquals("11", days.first().dayOfMonth)
    assertEquals(listOf(false, false, false, true, false, false, false), days.map { it.isToday })
  }

  private suspend fun kotlinx.coroutines.test.TestScope.stateFor(vararg items: TodayItem): DayOverviewState {
    every { observeTodayScheduleUseCase() } returns flowOf(TodaySnapshot(items.toList(), now))
    val viewModel = DayOverviewViewModel(
      dispatcherProvider = mockDispatcherProvider(),
      observeTodayScheduleUseCase = observeTodayScheduleUseCase,
      todayItemHomeEventMapper = todayItemHomeEventMapper,
      dateTimeManager = dateTimeManager,
      textProvider = textProvider,
    )
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect { } }
    return viewModel.state.value
  }

  private fun formatArgs(args: List<Any?>): List<Any?> =
    args.drop(1).flatMap { if (it is Array<*>) it.toList() else listOf(it) }

  private fun timed(hour: Int, minute: Int): TodayItem =
    TodayItem.Birthday(mockk(), today.atTime(hour, minute), TodayBucket.Timed)

  private fun allDay(): TodayItem =
    TodayItem.Birthday(mockk(), today.atStartOfDay(), TodayBucket.AllDay)

  private fun overdue(): TodayItem =
    TodayItem.Reminder(mockk(), today.minusDays(1).atTime(9, 0), isOverdue = true)

  private fun homeEvent(text: String) = HomeEvent(
    id = "id",
    text = text,
    description = null,
    groupName = null,
    remaining = null,
    color = androidx.compose.ui.graphics.Color.Black,
    action = null,
    date = today,
    time = LocalTime.NOON,
    type = HomeEvent.EventType.Birthday,
  )
}
