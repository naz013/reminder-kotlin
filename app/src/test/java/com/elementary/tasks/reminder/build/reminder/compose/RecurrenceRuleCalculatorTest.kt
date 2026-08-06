package com.elementary.tasks.reminder.build.reminder.compose

import com.elementary.tasks.BaseTest
import com.elementary.tasks.reminder.build.ArrivingCoordinatesBuilderItem
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.DateBuilderItem
import com.elementary.tasks.reminder.build.DayOfMonthBuilderItem
import com.elementary.tasks.reminder.build.DayOfYearBuilderItem
import com.elementary.tasks.reminder.build.DaysOfWeekBuilderItem
import com.elementary.tasks.reminder.build.LeavingCoordinatesBuilderItem
import com.elementary.tasks.reminder.build.LocationDelayDateBuilderItem
import com.elementary.tasks.reminder.build.LocationDelayTimeBuilderItem
import com.elementary.tasks.reminder.build.RepeatIntervalBuilderItem
import com.elementary.tasks.reminder.build.RepeatLimitBuilderItem
import com.elementary.tasks.reminder.build.RepeatTimeBuilderItem
import com.elementary.tasks.reminder.build.SubTasksBuilderItem
import com.elementary.tasks.reminder.build.TimeBuilderItem
import com.elementary.tasks.reminder.build.TimerBuilderItem
import com.elementary.tasks.reminder.build.bi.BiGroup
import com.elementary.tasks.reminder.build.bi.ProcessedBuilderItems
import com.elementary.tasks.reminder.build.reminder.EventData
import com.elementary.tasks.reminder.build.reminder.ICalDateTimeCalculator
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.datecalc.RecurrenceCalculator
import com.github.naz013.domain.Place
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.sync.SyncState
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class RecurrenceRuleCalculatorTest : BaseTest() {
  private val dateTimeManager = mockk<DateTimeManager>()
  private val iCalDateTimeCalculator = mockk<ICalDateTimeCalculator>()
  private val recurrenceCalculator = mockk<RecurrenceCalculator>()

  private lateinit var calculator: RecurrenceRuleCalculator

  @Before
  override fun setUp() {
    super.setUp()
    every { dateTimeManager.localToUtc(any()) } answers { firstArg() }
    every { dateTimeManager.getCurrentDateTime() } returns NOW
    calculator = RecurrenceRuleCalculator(dateTimeManager, iCalDateTimeCalculator, recurrenceCalculator)
  }

  @Test
  fun `plain date and time with no repeat produces Once`() {
    val items =
      itemsOf(
        dateItem(NOW.toLocalDate()),
        timeItem(NOW.toLocalTime()),
      )

    val result = calculator(items)

    assertEquals(RecurrenceRule.Once, result?.rule)
    assertEquals(NOW, result?.schedule?.eventDateTime)
  }

  @Test
  fun `date and time with a repeat interval produces Daily`() {
    val items =
      itemsOf(
        dateItem(NOW.toLocalDate()),
        timeItem(NOW.toLocalTime()),
        repeatTimeItem(3600_000L),
        repeatLimitItem(5),
      )

    val result = calculator(items)

    assertEquals(RecurrenceRule.Daily(repeatInterval = 3600_000L, repeatLimit = 5), result?.rule)
  }

  @Test
  fun `countdown timer with no repeat produces Countdown with zero interval`() {
    every { recurrenceCalculator.getStartTimerDateTime(countdownTimeInMillis = 60_000L) } returns NOW

    val items = itemsOf(timerItem(60_000L))

    val result = calculator(items)

    assertEquals(RecurrenceRule.Countdown(after = 60_000L, repeatInterval = 0, repeatLimit = -1), result?.rule)
  }

  @Test
  fun `repeating countdown timer carries the repeat interval and limit`() {
    every { recurrenceCalculator.getStartTimerDateTime(countdownTimeInMillis = 60_000L) } returns NOW

    val items = itemsOf(timerItem(60_000L), repeatTimeItem(30_000L), repeatLimitItem(2))

    val result = calculator(items)

    assertEquals(RecurrenceRule.Countdown(after = 60_000L, repeatInterval = 30_000L, repeatLimit = 2), result?.rule)
  }

  @Test
  fun `weekdays with a time produces Weekly`() {
    every {
      recurrenceCalculator.findNextDayOfWeekDateTime(any(), any(), any())
    } returns NOW

    val items = itemsOf(timeItem(NOW.toLocalTime()), weekdaysItem(listOf(1, 3, 5)))

    val result = calculator(items)

    assertEquals(RecurrenceRule.Weekly(weekdays = listOf(1, 3, 5)), result?.rule)
  }

  @Test
  fun `day of month with a time produces Monthly`() {
    every {
      recurrenceCalculator.findNextMonthDayDateTime(any(), any(), any(), any())
    } returns NOW

    val items = itemsOf(timeItem(NOW.toLocalTime()), dayOfMonthItem(15), repeatIntervalItem(2), repeatLimitItem(4))

    val result = calculator(items)

    assertEquals(RecurrenceRule.Monthly(dayOfMonth = 15, repeatInterval = 2, repeatLimit = 4), result?.rule)
  }

  @Test
  fun `day of year with a time produces Yearly`() {
    every {
      recurrenceCalculator.findNextYearDayDateTime(any(), any(), any(), any(), any())
    } returns NOW

    val dayOfYear = LocalDate.of(LocalDate.now().year, 3, 10).dayOfYear
    val items = itemsOf(timeItem(NOW.toLocalTime()), dayOfYearItem(dayOfYear))

    val result = calculator(items)

    assertEquals(RecurrenceRule.Yearly(dayOfMonth = 10, monthOfYear = 2, repeatInterval = 1, repeatLimit = -1), result?.rule)
  }

  @Test
  fun `arriving coordinates with a complete future delay marks it as delayed`() {
    every { dateTimeManager.isCurrent(any<LocalDateTime>()) } returns true
    val place = Place(syncState = SyncState.Synced)

    val items =
      itemsOf(
        arrivingItem(place),
        locationDelayDateItem(NOW.toLocalDate()),
        locationDelayTimeItem(NOW.toLocalTime()),
      )

    val result = calculator(items)

    assertEquals(RecurrenceRule.LocationEnter, result?.rule)
    assertEquals(true, result?.location?.hasDelayedReminder)
    assertEquals(listOf(place), result?.places)
    assertEquals(NOW, result?.schedule?.eventDateTime)
  }

  @Test
  fun `leaving coordinates with an incomplete delay is marked delayed but has no event time`() {
    val place = Place(syncState = SyncState.Synced)
    val items = itemsOf(leavingItem(place), locationDelayDateItem(NOW.toLocalDate()))

    val result = calculator(items)

    assertEquals(RecurrenceRule.LocationExit, result?.rule)
    assertEquals(true, result?.location?.hasDelayedReminder)
    assertNull(result?.schedule?.eventDateTime)
  }

  @Test
  fun `arriving coordinates with no delay at all is not marked delayed`() {
    val place = Place(syncState = SyncState.Synced)
    val items = itemsOf(arrivingItem(place))

    val result = calculator(items)

    assertEquals(false, result?.location?.hasDelayedReminder)
    assertNull(result?.schedule?.eventDateTime)
  }

  @Test
  fun `iCalendar recurrence delegates to ICalDateTimeCalculator`() {
    every { iCalDateTimeCalculator(any()) } returns EventData(startDateTime = NOW, recurObject = "RRULE:FREQ=DAILY")

    // groupMap membership alone is enough for the calculator to attempt the iCal branch - no
    // need for real ICal builder items since ICalDateTimeCalculator itself is mocked above.
    val withIcalGroup = ProcessedBuilderItems(emptyList()).copy(groupMap = mapOf(BiGroup.ICAL to emptyList()))

    val result = calculator(withIcalGroup)

    assertEquals(RecurrenceRule.ICalendar("RRULE:FREQ=DAILY"), result?.rule)
    assertEquals(NOW, result?.schedule?.eventDateTime)
  }

  @Test
  fun `shopping list with no due date produces Once with a null event time`() {
    val items = itemsOf(subTasksItem())

    val result = calculator(items)

    assertEquals(RecurrenceRule.Once, result?.rule)
    assertNull(result?.schedule?.eventDateTime)
  }

  @Test
  fun `a lone date with no time is not a valid recurrence but still produces a schedule`() {
    val items = itemsOf(dateItem(NOW.toLocalDate()))

    val result = calculator(items)

    assertEquals(RecurrenceRule.Once, result?.rule)
    assertNull(result?.schedule?.eventDateTime)
  }

  @Test
  fun `nothing selected produces no recurrence at all`() {
    val result = calculator(ProcessedBuilderItems(emptyList()))

    assertNull(result)
  }

  @Test
  fun `conflicting recurrence selections produce no recurrence`() {
    every {
      recurrenceCalculator.findNextDayOfWeekDateTime(any(), any(), any())
    } returns NOW

    val items =
      itemsOf(
        dateItem(NOW.toLocalDate()),
        timeItem(NOW.toLocalTime()),
        weekdaysItem(listOf(1)),
      )

    val result = calculator(items)

    assertNull(result)
  }

  @Test
  fun `date and time with a zero repeat interval still produces Once`() {
    val items = itemsOf(dateItem(NOW.toLocalDate()), timeItem(NOW.toLocalTime()), repeatTimeItem(0L))

    val result = calculator(items)

    assertEquals(RecurrenceRule.Once, result?.rule)
  }

  @Test
  fun `date and time with only a repeat limit and no repeat interval stays Once`() {
    val items = itemsOf(dateItem(NOW.toLocalDate()), timeItem(NOW.toLocalTime()), repeatLimitItem(5))

    val result = calculator(items)

    assertEquals(RecurrenceRule.Once, result?.rule)
  }

  @Test
  fun `countdown timer with a zero duration produces no recurrence`() {
    val items = itemsOf(timerItem(0L))

    val result = calculator(items)

    assertNull(result)
  }

  @Test
  fun `weekdays with no day actually checked produces no recurrence`() {
    val items = itemsOf(timeItem(NOW.toLocalTime()), weekdaysItem(emptyList()))

    val result = calculator(items)

    assertNull(result)
  }

  @Test
  fun `weekdays with a repeat limit carries the limit onto Weekly`() {
    every {
      recurrenceCalculator.findNextDayOfWeekDateTime(any(), any(), any())
    } returns NOW

    val items = itemsOf(timeItem(NOW.toLocalTime()), weekdaysItem(listOf(1, 0, 1)), repeatLimitItem(3))

    val result = calculator(items)

    assertEquals(RecurrenceRule.Weekly(weekdays = listOf(1, 0, 1), repeatLimit = 3), result?.rule)
  }

  @Test
  fun `day of month without a time produces no recurrence`() {
    val items = itemsOf(dayOfMonthItem(15))

    val result = calculator(items)

    assertNull(result)
  }

  @Test
  fun `day of year with an out-of-range value produces no recurrence`() {
    // 367 is never a valid day-of-year, regardless of leap years, so this can't flake.
    val items = itemsOf(timeItem(NOW.toLocalTime()), dayOfYearItem(367))

    val result = calculator(items)

    assertNull(result)
  }

  @Test
  fun `arriving coordinates with a complete delay in the past is not treated as scheduled`() {
    every { dateTimeManager.isCurrent(any<LocalDateTime>()) } returns false
    val place = Place(syncState = SyncState.Synced)

    val items =
      itemsOf(
        arrivingItem(place),
        locationDelayDateItem(NOW.toLocalDate()),
        locationDelayTimeItem(NOW.toLocalTime()),
      )

    val result = calculator(items)

    assertEquals(RecurrenceRule.LocationEnter, result?.rule)
    assertEquals(true, result?.location?.hasDelayedReminder)
    assertEquals(NOW, result?.schedule?.startDateTime)
    assertNull(result?.schedule?.eventDateTime)
  }

  @Test
  fun `location delay alone without a place still resolves via the delay branch`() {
    every { dateTimeManager.isCurrent(any<LocalDateTime>()) } returns true

    val items = itemsOf(locationDelayDateItem(NOW.toLocalDate()), locationDelayTimeItem(NOW.toLocalTime()))

    val result = calculator(items)

    assertEquals(RecurrenceRule.LocationEnter, result?.rule)
    assertTrue(result?.places?.isEmpty() == true)
    assertEquals(NOW, result?.schedule?.eventDateTime)
  }

  @Test
  fun `both arriving and leaving coordinates present resolves to LocationEnter`() {
    val arrivingPlace = Place(syncState = SyncState.Synced)
    val leavingPlace = Place(syncState = SyncState.Synced)

    val items = itemsOf(arrivingItem(arrivingPlace), leavingItem(leavingPlace))

    val result = calculator(items)

    assertEquals(RecurrenceRule.LocationEnter, result?.rule)
    assertEquals(listOf(arrivingPlace), result?.places)
  }

  @Test
  fun `iCalendar recurrence returning no event data produces no recurrence`() {
    every { iCalDateTimeCalculator(any()) } returns null

    val withIcalGroup = ProcessedBuilderItems(emptyList()).copy(groupMap = mapOf(BiGroup.ICAL to emptyList()))

    val result = calculator(withIcalGroup)

    assertNull(result)
  }

  @Test
  fun `a lone time with no date is not a valid recurrence but still produces a schedule`() {
    val items = itemsOf(timeItem(NOW.toLocalTime()))

    val result = calculator(items)

    assertEquals(RecurrenceRule.Once, result?.rule)
    assertNull(result?.schedule?.eventDateTime)
  }

  @Test
  fun `countdown timer combined with day of month produces no recurrence`() {
    val items = itemsOf(timerItem(60_000L), dayOfMonthItem(10))

    val result = calculator(items)

    assertNull(result)
  }

  private fun itemsOf(vararg items: BuilderItem<*>) = ProcessedBuilderItems(items.toList())

  private fun dateItem(value: LocalDate) =
    DateBuilderItem(title = "d", description = null, dateFormatter = mockk(relaxed = true)).apply {
      modifier.update(value)
    }

  private fun timeItem(value: LocalTime) =
    TimeBuilderItem(title = "t", description = null, timeFormatter = mockk(relaxed = true)).apply {
      modifier.update(value)
    }

  private fun timerItem(value: Long) =
    TimerBuilderItem(title = "timer", description = null, timerFormatter = mockk(relaxed = true)).apply {
      modifier.update(value)
    }

  private fun repeatTimeItem(value: Long) =
    RepeatTimeBuilderItem(title = "rt", description = null, repeatTimeFormatter = mockk(relaxed = true)).apply {
      modifier.update(value)
    }

  private fun repeatIntervalItem(value: Long) =
    RepeatIntervalBuilderItem(title = "ri", description = null, repeatIntervalFormatter = mockk(relaxed = true)).apply {
      modifier.update(value)
    }

  private fun repeatLimitItem(value: Int) =
    RepeatLimitBuilderItem(title = "rl", description = null, repeatLimitFormatter = mockk(relaxed = true)).apply {
      modifier.update(value)
    }

  private fun weekdaysItem(value: List<Int>) =
    DaysOfWeekBuilderItem(title = "w", description = null, weekdayArrayFormatter = mockk(relaxed = true)).apply {
      modifier.update(value)
    }

  private fun dayOfMonthItem(value: Int) =
    DayOfMonthBuilderItem(title = "dom", description = null, dayOfMonthFormatter = mockk(relaxed = true)).apply {
      modifier.update(value)
    }

  private fun dayOfYearItem(value: Int) =
    DayOfYearBuilderItem(title = "doy", description = null, dayOfYearFormatter = mockk(relaxed = true)).apply {
      modifier.update(value)
    }

  private fun arrivingItem(value: Place) =
    ArrivingCoordinatesBuilderItem(title = "arr", description = null, placeFormatter = mockk(relaxed = true)).apply {
      modifier.update(value)
    }

  private fun leavingItem(value: Place) =
    LeavingCoordinatesBuilderItem(title = "leave", description = null, placeFormatter = mockk(relaxed = true)).apply {
      modifier.update(value)
    }

  private fun locationDelayDateItem(value: LocalDate) =
    LocationDelayDateBuilderItem(title = "ldd", description = null, dateFormatter = mockk(relaxed = true)).apply {
      modifier.update(value)
    }

  private fun locationDelayTimeItem(value: LocalTime) =
    LocationDelayTimeBuilderItem(title = "ldt", description = null, timeFormatter = mockk(relaxed = true)).apply {
      modifier.update(value)
    }

  private fun subTasksItem() =
    SubTasksBuilderItem(
      title = "shop",
      description = null,
      shopItemsFormatter = mockk(relaxed = true),
      dateTimeManager = dateTimeManager,
    ).apply {
      modifier.update(listOf(mockk(relaxed = true)))
    }

  companion object {
    private val NOW: LocalDateTime = LocalDateTime.of(2026, 7, 24, 10, 0)
  }
}
