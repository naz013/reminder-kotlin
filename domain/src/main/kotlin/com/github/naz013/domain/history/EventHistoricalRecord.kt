package com.github.naz013.domain.history

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

data class EventHistoricalRecord(
  val id: String,
  val eventId: String,
  val date: LocalDate,
  val time: LocalTime,
  val type: EventHistoricalRecordType
) {

  fun getDateTime(): LocalDateTime {
    return LocalDateTime.of(date, time)
  }
}
