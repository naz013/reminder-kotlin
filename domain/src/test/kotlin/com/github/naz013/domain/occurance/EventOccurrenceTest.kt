package com.github.naz013.domain.occurance

import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class EventOccurrenceTest {

  @Test
  fun `getDateTime combines the date and time fields`() {
    val occurrence = EventOccurrence(
      id = "id",
      eventId = "event-id",
      date = LocalDate.of(2023, 6, 17),
      time = LocalTime.of(9, 30),
      type = OccurrenceType.Birthday
    )

    assertEquals(LocalDateTime.of(2023, 6, 17, 9, 30), occurrence.getDateTime())
  }
}
