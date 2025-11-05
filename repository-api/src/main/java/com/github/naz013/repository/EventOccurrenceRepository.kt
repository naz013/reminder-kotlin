package com.github.naz013.repository

import com.github.naz013.domain.occurance.EventOccurrence
import org.threeten.bp.LocalDate

interface EventOccurrenceRepository {
  suspend fun save(occurrence: EventOccurrence)

  suspend fun getByDateRange(startDate: LocalDate, endDate: LocalDate): List<EventOccurrence>
  suspend fun getByEventId(eventId: String): List<EventOccurrence>

  suspend fun deleteById(id: String)
  suspend fun deleteByEventId(eventId: String)
  suspend fun deleteAll()
}
