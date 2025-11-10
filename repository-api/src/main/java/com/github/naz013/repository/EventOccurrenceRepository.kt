package com.github.naz013.repository

import com.github.naz013.domain.occurance.EventOccurrence
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

interface EventOccurrenceRepository {
  suspend fun save(occurrence: EventOccurrence)
  suspend fun saveAll(occurrences: List<EventOccurrence>)

  suspend fun getByDateRange(startDate: LocalDate, endDate: LocalDate): List<EventOccurrence>
  suspend fun getByEventId(eventId: String): List<EventOccurrence>
  suspend fun getByDateAndTimeRange(
    date: LocalDate,
    startTime: LocalTime,
    endTime: LocalTime
  ): List<EventOccurrence>

  suspend fun deleteById(id: String)
  suspend fun deleteByEventId(eventId: String)
  suspend fun deleteAll()
}
