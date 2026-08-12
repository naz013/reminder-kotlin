package com.github.naz013.repository.entity

import com.github.naz013.domain.occurance.EventOccurrence
import com.github.naz013.domain.occurance.OccurrenceType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

class EventOccurrenceEntityTest {

  @Test
  fun `constructor encodes date as epoch day and time as second of day`() {
    val occurrence = EventOccurrence(
      id = "id",
      eventId = "event-1",
      date = LocalDate.of(2023, 6, 17),
      time = LocalTime.of(9, 30),
      type = OccurrenceType.CalendarEvent
    )

    val entity = EventOccurrenceEntity(occurrence)

    assertEquals(LocalDate.of(2023, 6, 17).toEpochDay(), entity.date)
    assertEquals(9 * 3600L + 30 * 60L, entity.time)
    assertEquals("CalendarEvent", entity.type)
  }

  @Test
  fun `round trip through domain preserves the date and time`() {
    val occurrence = EventOccurrence(
      id = "id",
      eventId = "event-1",
      date = LocalDate.of(2023, 6, 17),
      time = LocalTime.of(9, 30),
      type = OccurrenceType.Reminder
    )

    val restored = EventOccurrenceEntity(occurrence).toDomain()

    assertEquals(occurrence, restored)
  }
}
