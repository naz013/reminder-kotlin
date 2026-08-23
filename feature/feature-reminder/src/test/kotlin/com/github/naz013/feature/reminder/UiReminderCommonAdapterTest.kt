package com.github.naz013.feature.reminder

import com.github.naz013.logic.reminder.ReminderPreferences
import com.github.naz013.common.PackageManagerWrapper
import com.github.naz013.common.TextProvider
import com.github.naz013.common.contacts.ContactsReader
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.icalendar.ICalendarApi
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

/**
 * Regression guard for a previously-shipped bug: `ReminderV2.schedule.eventDateTime` is stored
 * UTC-zoned, but [UiReminderCommonAdapter.getDueV2] was feeding it directly into
 * [DateTimeManager] calls that expect a local-zoned [LocalDateTime], showing/scheduling the wrong
 * time for any device not set to UTC+0.
 */
class UiReminderCommonAdapterTest {
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val dateTimeManager = mockk<DateTimeManager>(relaxed = true)
  private val contactsReader = mockk<ContactsReader>(relaxed = true)
  private val packageManagerWrapper = mockk<PackageManagerWrapper>(relaxed = true)
  private val iCalendarApi = mockk<ICalendarApi>(relaxed = true)
  private val reminderPreferences = mockk<ReminderPreferences>(relaxed = true)
  private val modelDateTimeFormatter = mockk<ModelDateTimeFormatter>(relaxed = true)

  private lateinit var adapter: UiReminderCommonAdapter

  @Before
  fun setUp() {
    adapter = UiReminderCommonAdapter(
      textProvider = textProvider,
      dateTimeManager = dateTimeManager,
      contactsReader = contactsReader,
      packageManagerWrapper = packageManagerWrapper,
      iCalendarApi = iCalendarApi,
      reminderPreferences = reminderPreferences,
      modelDateTimeFormatter = modelDateTimeFormatter,
    )
  }

  @Test
  fun `getDueV2 converts the UTC-zoned schedule time to local before formatting or computing millis`() {
    val utcEventDateTime = LocalDateTime.of(2026, 7, 22, 9, 0, 0)
    val localEventDateTime = LocalDateTime.of(2026, 7, 22, 12, 0, 0) // e.g. this device is UTC+3
    every { dateTimeManager.utcToLocal(utcEventDateTime) } returns localEventDateTime
    every { dateTimeManager.getFullDateTime(localEventDateTime) } returns "22 Jul 2026, 12:00"
    every { dateTimeManager.toMillis(localEventDateTime) } returns 999L
    every { dateTimeManager.getTime(localEventDateTime.toLocalTime()) } returns "12:00"
    every { dateTimeManager.getCurrentDateTime() } returns localEventDateTime
    every { modelDateTimeFormatter.getRemaining(any<LocalDateTime>(), any<LocalDateTime>()) } returns "remaining"

    val reminder = ReminderV2(
      schedule = ReminderSchedule(startDateTime = utcEventDateTime, eventDateTime = utcEventDateTime),
    )

    val result = adapter.getDueV2(reminder)

    assertEquals(localEventDateTime, result.localDateTime)
    assertEquals("22 Jul 2026, 12:00", result.formattedDateTime)
    assertEquals("12:00", result.formattedTime)
    assertEquals(999L, result.millis)
  }

  @Test
  fun `getDueV2 leaves due fields null when there is no scheduled event time`() {
    val reminder = ReminderV2(schedule = ReminderSchedule(startDateTime = LocalDateTime.now(), eventDateTime = null))

    val result = adapter.getDueV2(reminder)

    assertEquals(null, result.localDateTime)
    assertEquals(null, result.formattedDateTime)
    assertEquals(0L, result.millis)
  }

  @Test
  fun `getRepeatLimitInfoV2 returns null for an unlimited reminder`() {
    val reminder = reminderV2(recurrence = RecurrenceRule.Once)

    assertNull(adapter.getRepeatLimitInfoV2(reminder))
  }

  @Test
  fun `getRepeatLimitInfoV2 combines progress and remaining count while the limit hasn't been reached`() {
    every { textProvider.getText(R.string.repeat_progress, 3, 10) } returns "3 of 10 times"
    every { textProvider.getText(R.string.repeat_times_left, 7) } returns "7 left"
    val reminder = reminderV2(recurrence = RecurrenceRule.Daily(repeatLimit = 10), eventCount = 3)

    val result = adapter.getRepeatLimitInfoV2(reminder)

    assertEquals("3 of 10 times · 7 left", result?.text)
    assertEquals(false, result?.isLimitReached)
  }

  @Test
  fun `getRepeatLimitInfoV2 flags the limit as reached and clamps the used count to the limit`() {
    every { textProvider.getText(R.string.repeat_limit_reached) } returns "Repeat limit reached"
    every { textProvider.getText(R.string.repeat_progress, 10, 10) } returns "10 of 10 times"
    // eventCount overshoots the limit - the scheduler stops firing once it's hit, but the count
    // shown to the user should still clamp to the limit rather than read "12 of 10".
    val reminder = reminderV2(recurrence = RecurrenceRule.Daily(repeatLimit = 10), eventCount = 12)

    val result = adapter.getRepeatLimitInfoV2(reminder)

    assertEquals("Repeat limit reached · 10 of 10 times", result?.text)
    assertEquals(true, result?.isLimitReached)
  }

  @Test
  fun `getRepeatUntilV2 returns null when the recurrence has no end date`() {
    assertNull(adapter.getRepeatUntilV2(RecurrenceRule.Once))
    assertNull(adapter.getRepeatUntilV2(RecurrenceRule.Daily(until = null)))
  }

  @Test
  fun `getRepeatUntilV2 converts the UTC-zoned until date to local before formatting`() {
    val utcUntil = LocalDateTime.of(2026, 12, 31, 21, 0, 0)
    val localUntil = LocalDateTime.of(2027, 1, 1, 0, 0, 0) // e.g. this device is UTC+3
    every { dateTimeManager.utcToLocal(utcUntil) } returns localUntil
    every { dateTimeManager.getDate(localUntil.toLocalDate()) } returns "1 Jan 2027"
    every { textProvider.getText(R.string.repeats_until, "1 Jan 2027") } returns "Repeats until 1 Jan 2027"

    val result = adapter.getRepeatUntilV2(RecurrenceRule.Weekly(weekdays = listOf(1, 0, 0, 0, 0, 0, 0), until = utcUntil))

    assertEquals("Repeats until 1 Jan 2027", result)
  }

  @Test
  fun `getTriggeredCountTextV2 returns null for a limited reminder`() {
    val reminder = reminderV2(recurrence = RecurrenceRule.Daily(repeatLimit = 10), eventCount = 3)

    assertNull(adapter.getTriggeredCountTextV2(reminder))
  }

  @Test
  fun `getTriggeredCountTextV2 returns null when the reminder has never fired`() {
    val reminder = reminderV2(recurrence = RecurrenceRule.Once, eventCount = 0)

    assertNull(adapter.getTriggeredCountTextV2(reminder))
  }

  @Test
  fun `getTriggeredCountTextV2 formats the trigger count for an unlimited reminder that has fired`() {
    every { textProvider.getText(R.string.reminder_triggered_times, 5L) } returns "Triggered 5 times"
    val reminder = reminderV2(recurrence = RecurrenceRule.Once, eventCount = 5)

    assertEquals("Triggered 5 times", adapter.getTriggeredCountTextV2(reminder))
  }

  @Test
  fun `getSnoozedCountTextV2 returns null when the reminder has never been snoozed`() {
    val reminder = reminderV2(snoozeCount = 0)

    assertNull(adapter.getSnoozedCountTextV2(reminder))
  }

  @Test
  fun `getSnoozedCountTextV2 formats the snooze count`() {
    every { textProvider.getText(R.string.reminder_snoozed_times, 2L) } returns "Snoozed 2 times"
    val reminder = reminderV2(snoozeCount = 2)

    assertEquals("Snoozed 2 times", adapter.getSnoozedCountTextV2(reminder))
  }

  private fun reminderV2(
    recurrence: RecurrenceRule = RecurrenceRule.Once,
    eventCount: Long = 0,
    snoozeCount: Long = 0,
  ): ReminderV2 =
    ReminderV2(
      recurrence = recurrence,
      schedule = ReminderSchedule(startDateTime = LocalDateTime.now()),
      eventCount = eventCount,
      snoozeCount = snoozeCount,
    )
}
