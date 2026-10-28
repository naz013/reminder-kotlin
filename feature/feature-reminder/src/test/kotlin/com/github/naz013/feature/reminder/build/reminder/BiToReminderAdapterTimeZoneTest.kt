package com.github.naz013.feature.reminder.build.reminder

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.datecalc.RecurrenceCalculator
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.DateBuilderItem
import com.github.naz013.feature.reminder.build.TimeBuilderItem
import com.github.naz013.feature.reminder.build.TimeZoneBuilderItem
import com.github.naz013.feature.reminder.build.TimerBuilderItem
import com.github.naz013.feature.reminder.build.logic.builderstate.BuilderStateCalculator
import com.github.naz013.feature.reminder.build.reminder.compose.CalendarExportCalculator
import com.github.naz013.feature.reminder.build.reminder.compose.RecurrenceRuleCalculator
import com.github.naz013.feature.reminder.build.reminder.compose.ReminderActionCalculator
import com.github.naz013.feature.reminder.build.reminder.validation.EventTimeValidator
import com.github.naz013.feature.reminder.build.reminder.validation.ReminderValidator
import com.github.naz013.feature.reminder.build.reminder.validation.SubTasksValidator
import com.github.naz013.feature.reminder.build.reminder.validation.TargetValidator
import com.github.naz013.feature.reminder.build.timezone.TimeZoneCatalog
import com.github.naz013.testing.BaseTest
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

class BiToReminderAdapterTimeZoneTest : BaseTest() {
  private val dateTimeManager = mockk<DateTimeManager>()
  private val recurrenceCalculator = mockk<RecurrenceCalculator>()
  private val berlin = ZoneId.of("Europe/Berlin")

  private lateinit var adapter: BiToReminderAdapter

  @Before
  override fun setUp() {
    super.setUp()
    every { dateTimeManager.localToUtc(any()) } answers { firstArg() }
    every { dateTimeManager.localToUtc(any(), berlin) } answers { firstArg<LocalDateTime>().minusHours(2) }
    every { dateTimeManager.getCurrentDateTime() } returns NOW
    every { recurrenceCalculator.getStartTimerDateTime(countdownTimeInMillis = 60_000L) } returns NOW
    adapter =
      BiToReminderAdapter(
        builderStateCalculator = BuilderStateCalculator(),
        reminderValidator = ReminderValidator(TargetValidator(), SubTasksValidator(), EventTimeValidator()),
        recurrenceRuleCalculator =
          RecurrenceRuleCalculator(dateTimeManager, mockk<ICalDateTimeCalculator>(), recurrenceCalculator),
        reminderActionCalculator = ReminderActionCalculator(),
        calendarExportCalculator = CalendarExportCalculator(),
      )
  }

  @Test
  fun `saves the picked zone id on a date and time reminder`() {
    val items = listOf(dateItem(), timeItem(), zoneItem("Europe/Berlin"))

    val result = adapter(baseReminder(), items, isEdited = false)

    assertEquals("Europe/Berlin", (result as BiToReminderAdapter.BuildResult.Success).reminderV2.timeZoneId)
  }

  @Test
  fun `saves a floating reminder when the zone item has no value`() {
    val items = listOf(dateItem(), timeItem(), zoneItem(null))

    val result = adapter(baseReminder(timeZoneId = "Europe/Berlin"), items, isEdited = true)

    assertNull((result as BiToReminderAdapter.BuildResult.Success).reminderV2.timeZoneId)
  }

  @Test
  fun `clears a previously stored zone when the zone item is removed`() {
    val items = listOf(dateItem(), timeItem())

    val result = adapter(baseReminder(timeZoneId = "Europe/Berlin"), items, isEdited = true)

    assertNull((result as BiToReminderAdapter.BuildResult.Success).reminderV2.timeZoneId)
  }

  @Test
  fun `stores no zone for a countdown reminder even when a zone item is present`() {
    val timer = TimerBuilderItem(title = "t", description = null, timerFormatter = mockk(relaxed = true))
    timer.modifier.update(60_000L)

    val result = adapter(baseReminder(), listOf(timer, zoneItem("Europe/Berlin")), isEdited = false)

    val reminder = (result as BiToReminderAdapter.BuildResult.Success).reminderV2
    assertEquals(RecurrenceRule.Countdown(after = 60_000L), reminder.recurrence)
    assertNull(reminder.timeZoneId)
  }

  private fun baseReminder(timeZoneId: String? = null) =
    ReminderV2(schedule = ReminderSchedule(startDateTime = NOW), timeZoneId = timeZoneId)

  private fun dateItem(): BuilderItem<*> =
    DateBuilderItem(title = "d", description = null, dateFormatter = mockk(relaxed = true)).apply {
      modifier.update(NOW.toLocalDate())
    }

  private fun timeItem(): BuilderItem<*> =
    TimeBuilderItem(title = "t", description = null, timeFormatter = mockk(relaxed = true)).apply {
      modifier.update(NOW.toLocalTime())
    }

  private fun zoneItem(zoneId: String?): BuilderItem<*> =
    TimeZoneBuilderItem(title = "z", description = null, catalog = TimeZoneCatalog("Device")).apply {
      modifier.update(zoneId)
    }

  private companion object {
    val NOW: LocalDateTime = LocalDateTime.of(2026, 7, 24, 10, 0)
  }
}
