package com.github.naz013.repository.entity

import com.github.naz013.domain.history.EventHistoricalRecord
import com.github.naz013.domain.history.EventHistoricalRecordType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

class EventHistoryEntityTest {

  @Test
  fun `constructor encodes date as epoch day and time as second of day`() {
    val record = EventHistoricalRecord(
      id = "id",
      eventId = "event-1",
      date = LocalDate.of(2023, 6, 17),
      time = LocalTime.of(9, 30),
      type = EventHistoricalRecordType.Birthday
    )

    val entity = EventHistoryEntity(record)

    assertEquals(LocalDate.of(2023, 6, 17).toEpochDay(), entity.date)
    assertEquals(9 * 3600L + 30 * 60L, entity.time)
    assertEquals("Birthday", entity.type)
  }

  @Test
  fun `round trip through domain preserves the date and time`() {
    val record = EventHistoricalRecord(
      id = "id",
      eventId = "event-1",
      date = LocalDate.of(2023, 6, 17),
      time = LocalTime.of(9, 30),
      type = EventHistoricalRecordType.Reminder
    )

    val restored = EventHistoryEntity(record).toDomain()

    assertEquals(record, restored)
  }
}
