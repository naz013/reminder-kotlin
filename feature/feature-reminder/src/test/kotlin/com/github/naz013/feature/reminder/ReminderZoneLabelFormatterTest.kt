package com.github.naz013.feature.reminder

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import java.util.TimeZone

class ReminderZoneLabelFormatterTest {
  private val dateTimeManager = mockk<DateTimeManager>()
  private val berlin = ZoneId.of("Europe/Berlin")
  private val previousDefault: TimeZone = TimeZone.getDefault()
  private lateinit var formatter: ReminderZoneLabelFormatter

  @Before
  fun setUp() {
    TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
    every { dateTimeManager.utcToLocal(UTC, berlin) } returns LocalDateTime.of(2026, 7, 1, 9, 0)
    every { dateTimeManager.getTime(LocalTime.of(9, 0)) } returns "09:00"
    formatter = ReminderZoneLabelFormatter(dateTimeManager)
  }

  @After
  fun tearDown() {
    TimeZone.setDefault(previousDefault)
  }

  @Test
  fun `shows the time in the fixed zone when it differs from the device zone`() {
    assertEquals("09:00 Berlin", formatter.format(reminder("Europe/Berlin")))
  }

  @Test
  fun `shows nothing for a floating reminder`() {
    assertNull(formatter.format(reminder(null)))
  }

  @Test
  fun `shows nothing when the fixed zone is the device zone`() {
    TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"))

    assertNull(ReminderZoneLabelFormatter(dateTimeManager).format(reminder("Europe/Berlin")))
  }

  @Test
  fun `shows nothing for an unknown zone id`() {
    assertNull(formatter.format(reminder("Bogus/Zone")))
  }

  @Test
  fun `shows nothing for a rule that does not support a zone`() {
    assertNull(formatter.format(reminder("Europe/Berlin").copy(recurrence = RecurrenceRule.Countdown(after = 1000L))))
  }

  @Test
  fun `shows nothing when the reminder has no event time`() {
    assertNull(formatter.format(reminder("Europe/Berlin").copy(schedule = ReminderSchedule(startDateTime = UTC))))
  }

  private fun reminder(zoneId: String?) =
    ReminderV2(
      schedule = ReminderSchedule(startDateTime = UTC, eventDateTime = UTC),
      timeZoneId = zoneId,
    )

  private companion object {
    val UTC: LocalDateTime = LocalDateTime.of(2026, 7, 1, 7, 0)
  }
}
