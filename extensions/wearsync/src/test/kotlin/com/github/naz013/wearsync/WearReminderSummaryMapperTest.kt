package com.github.naz013.wearsync

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Place
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class WearReminderSummaryMapperTest {

  private lateinit var dateTimeManager: DateTimeManager
  private lateinit var mapper: WearReminderSummaryMapper

  @Before
  fun setup() {
    dateTimeManager = mockk()
    mapper = WearReminderSummaryMapper(dateTimeManager)
  }

  private fun reminder(
    eventDateTime: LocalDateTime? = null,
    places: List<Place> = emptyList(),
  ) = ReminderV2(
    summary = "Call the dentist",
    schedule = ReminderSchedule(startDateTime = LocalDateTime.now(), eventDateTime = eventDateTime),
    places = places,
  )

  @Test
  fun `maps title and due time from event date time`() {
    val utc = LocalDateTime.of(2026, 1, 1, 12, 0)
    val local = LocalDateTime.of(2026, 1, 1, 14, 0)
    every { dateTimeManager.utcToLocal(utc) } returns local
    every { dateTimeManager.toMillis(local) } returns 123L

    val summary = mapper.toSummary(reminder(eventDateTime = utc))

    assertEquals("Call the dentist", summary.title)
    assertEquals(123L, summary.eventDateTimeMillis)
  }

  @Test
  fun `maps null due time when reminder has no event date time`() {
    val summary = mapper.toSummary(reminder(eventDateTime = null))

    assertNull(summary.eventDateTimeMillis)
  }

  @Test
  fun `allows snooze when reminder has no places`() {
    val summary = mapper.toSummary(reminder(places = emptyList()))

    assertTrue(summary.canSnooze)
  }

  @Test
  fun `disallows snooze for location-based reminders`() {
    val summary = mapper.toSummary(reminder(places = listOf(mockk())))

    assertFalse(summary.canSnooze)
  }
}
