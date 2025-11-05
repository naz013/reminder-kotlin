package com.github.naz013.domain.occurance

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

data class EventOccurrence(
  val id: String,
  val eventId: String,
  val date: LocalDate,
  val time: LocalTime,
  val type: OccurrenceType
)
