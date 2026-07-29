package com.elementary.tasks.reminder.build.reminder

import com.elementary.tasks.BaseTest
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.DateBuilderItem
import com.elementary.tasks.reminder.build.DayOfMonthBuilderItem
import com.elementary.tasks.reminder.build.LeavingCoordinatesBuilderItem
import com.elementary.tasks.reminder.build.LocationDelayDateBuilderItem
import com.elementary.tasks.reminder.build.SummaryBuilderItem
import com.elementary.tasks.reminder.build.TimeBuilderItem
import com.elementary.tasks.reminder.build.TimerBuilderItem
import com.elementary.tasks.reminder.build.logic.builderstate.BuilderStateCalculator
import com.elementary.tasks.reminder.build.reminder.compose.CalendarExportCalculator
import com.elementary.tasks.reminder.build.reminder.compose.ReminderActionCalculator
import com.elementary.tasks.reminder.build.reminder.compose.RecurrenceRuleCalculator
import com.elementary.tasks.reminder.build.reminder.validation.EventTimeValidator
import com.elementary.tasks.reminder.build.reminder.validation.ReminderValidator
import com.elementary.tasks.reminder.build.reminder.validation.SubTasksValidator
import com.elementary.tasks.reminder.build.reminder.validation.TargetValidator
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.datecalc.RecurrenceCalculator
import com.github.naz013.domain.Place
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.reminder.v2.BuilderSchemeItemV2
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
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

class BiToReminderAdapterTest : BaseTest() {
  private val dateTimeManager = mockk<DateTimeManager>()
  private val iCalDateTimeCalculator = mockk<ICalDateTimeCalculator>()
  private val recurrenceCalculator = mockk<RecurrenceCalculator>()

  private lateinit var adapter: BiToReminderAdapter

  @Before
  override fun setUp() {
    super.setUp()
    every { dateTimeManager.localToUtc(any()) } answers { firstArg() }
    every { dateTimeManager.getCurrentDateTime() } returns NOW

    adapter =
      BiToReminderAdapter(
        builderStateCalculator = BuilderStateCalculator(),
        reminderValidator = ReminderValidator(TargetValidator(), SubTasksValidator(), EventTimeValidator()),
        recurrenceRuleCalculator = RecurrenceRuleCalculator(dateTimeManager, iCalDateTimeCalculator, recurrenceCalculator),
        reminderActionCalculator = ReminderActionCalculator(),
        calendarExportCalculator = CalendarExportCalculator(),
      )
  }

  @Test
  fun `a valid date and time reminder builds successfully with the computed event time`() {
    val items = itemsOf(dateItem(NOW.toLocalDate()), timeItem(NOW.toLocalTime()))

    val result = adapter(baseReminder(), items, isEdited = false)

    val success = result as? BiToReminderAdapter.BuildResult.Success
    assertEquals(RecurrenceRule.Once, success?.reminderV2?.recurrence)
    assertEquals(NOW, success?.reminderV2?.schedule?.eventDateTime)
  }

  @Test
  fun `no builder items at all fails to build`() {
    val result = adapter(baseReminder(), emptyList(), isEdited = false)

    assertTrue(result is BiToReminderAdapter.BuildResult.Error)
  }

  @Test
  fun `conflicting builder items that yield no recurrence fail to build`() {
    val items = itemsOf(timerItem(60_000L), dayOfMonthItem(10))

    val result = adapter(baseReminder(), items, isEdited = false)

    assertTrue(result is BiToReminderAdapter.BuildResult.Error)
  }

  @Test
  fun `a delayed location reminder with an incomplete delay fails validation`() {
    // Delay date without a delay time: hasDelayedReminder = true but eventDateTime stays null,
    // which EventTimeValidator rejects even though RecurrenceRuleCalculator itself produced a
    // recurrence.
    val place = Place(syncState = SyncState.Synced)
    val items = itemsOf(leavingItem(place), locationDelayDateItem(NOW.toLocalDate()))

    val result = adapter(baseReminder(), items, isEdited = false)

    assertTrue(result is BiToReminderAdapter.BuildResult.Error)
  }

  @Test
  fun `building resets transient fields from the base reminder and re-applies them from items`() {
    val items = itemsOf(dateItem(NOW.toLocalDate()), timeItem(NOW.toLocalTime()), summaryItem("Buy milk"))

    val result = adapter(baseReminder(summary = "stale summary"), items, isEdited = false)

    val success = result as? BiToReminderAdapter.BuildResult.Success
    assertEquals("Buy milk", success?.reminderV2?.summary)
  }

  @Test
  fun `builderScheme mirrors the item list order and biType`() {
    val items = itemsOf(dateItem(NOW.toLocalDate()), timeItem(NOW.toLocalTime()), summaryItem("Buy milk"))

    val result = adapter(baseReminder(), items, isEdited = false)

    val success = result as? BiToReminderAdapter.BuildResult.Success
    val expectedScheme = items.mapIndexed { index, item -> BuilderSchemeItemV2(item.biType.ordinal, index) }
    assertEquals(expectedScheme, success?.reminderV2?.builderScheme)
  }

  private fun baseReminder(summary: String = "") =
    ReminderV2(summary = summary, schedule = ReminderSchedule(startDateTime = NOW))

  private fun itemsOf(vararg items: BuilderItem<*>) = items.toList()

  private fun dateItem(value: LocalDate) =
    DateBuilderItem(title = "d", description = null, dateFormatter = mockk(relaxed = true)).apply {
      modifier.update(value)
    }

  private fun timeItem(value: LocalTime) =
    TimeBuilderItem(title = "t", description = null, timeFormatter = mockk(relaxed = true)).apply {
      modifier.update(value)
    }

  private fun summaryItem(value: String) =
    SummaryBuilderItem(title = "s", description = null).apply {
      modifier.update(value)
    }

  private fun timerItem(value: Long) =
    TimerBuilderItem(title = "timer", description = null, timerFormatter = mockk(relaxed = true)).apply {
      modifier.update(value)
    }

  private fun dayOfMonthItem(value: Int) =
    DayOfMonthBuilderItem(title = "dom", description = null, dayOfMonthFormatter = mockk(relaxed = true)).apply {
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

  companion object {
    private val NOW: LocalDateTime = LocalDateTime.of(2026, 7, 24, 10, 0)
  }
}
