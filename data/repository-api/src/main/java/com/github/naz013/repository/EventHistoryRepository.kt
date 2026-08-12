package com.github.naz013.repository

import com.github.naz013.domain.history.EventHistoricalRecord
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

interface EventHistoryRepository {
  suspend fun save(historicalRecord: EventHistoricalRecord)
  suspend fun saveAll(records: List<EventHistoricalRecord>)

  suspend fun getByDateRange(startDate: LocalDate, endDate: LocalDate): List<EventHistoricalRecord>
  suspend fun getByEventId(eventId: String): List<EventHistoricalRecord>
  suspend fun getByDateAndTimeRange(
    date: LocalDate,
    startTime: LocalTime,
    endTime: LocalTime
  ): List<EventHistoricalRecord>

  suspend fun deleteById(id: String)
  suspend fun deleteByEventId(eventId: String)
  suspend fun deleteAll()
}
