package com.github.naz013.domain.history

import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class EventHistoricalRecordTest {

  @Test
  fun `getDateTime combines the date and time fields`() {
    val record = EventHistoricalRecord(
      id = "id",
      eventId = "event-id",
      date = LocalDate.of(2023, 6, 17),
      time = LocalTime.of(9, 30),
      type = EventHistoricalRecordType.Reminder
    )

    assertEquals(LocalDateTime.of(2023, 6, 17, 9, 30), record.getDateTime())
  }
}
