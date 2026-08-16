package com.github.naz013.feature.reminder

import com.github.naz013.logic.reminder.ReminderPreferences
import com.github.naz013.common.PackageManagerWrapper
import com.github.naz013.common.TextProvider
import com.github.naz013.common.contacts.ContactsReader
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.icalendar.ICalendarApi
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
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
}
