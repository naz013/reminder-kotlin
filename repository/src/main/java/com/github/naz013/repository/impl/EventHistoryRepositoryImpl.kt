package com.github.naz013.repository.impl

import com.github.naz013.domain.history.EventHistoricalRecord
import com.github.naz013.repository.EventHistoryRepository
import com.github.naz013.repository.dao.EventHistoryDao
import com.github.naz013.repository.entity.EventHistoryEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

internal class EventHistoryRepositoryImpl(
  private val dao: EventHistoryDao,
  private val tableChangeNotifier: TableChangeNotifier
) : EventHistoryRepository {

  override suspend fun save(historicalRecord: EventHistoricalRecord) {
    dao.insert(EventHistoryEntity(historicalRecord))
    tableChangeNotifier.notify(Table.EventHistory)
  }

  override suspend fun saveAll(records: List<EventHistoricalRecord>) {
    dao.insertAll(records.map { EventHistoryEntity(it) })
    tableChangeNotifier.notify(Table.EventHistory)
  }

  override suspend fun getByDateRange(
    startDate: LocalDate,
    endDate: LocalDate
  ): List<EventHistoricalRecord> {
    return dao.getByDateRange(startDate.toEpochDay(), endDate.toEpochDay()).map { it.toDomain() }
  }

  override suspend fun getByEventId(eventId: String): List<EventHistoricalRecord> {
    return dao.getByEventId(eventId).map { it.toDomain() }
  }

  override suspend fun getByDateAndTimeRange(
    date: LocalDate,
    startTime: LocalTime,
    endTime: LocalTime
  ): List<EventHistoricalRecord> {
    return dao.getByDateAndTimeRange(
      date.toEpochDay(),
      startTime.toSecondOfDay(),
      endTime.toSecondOfDay()
    ).map { it.toDomain() }
  }

  override suspend fun deleteById(id: String) {
    dao.deleteById(id)
    tableChangeNotifier.notify(Table.EventHistory)
  }

  override suspend fun deleteByEventId(eventId: String) {
    dao.deleteByEventId(eventId)
    tableChangeNotifier.notify(Table.EventHistory)
  }

  override suspend fun deleteAll() {
    dao.deleteAll()
    tableChangeNotifier.notify(Table.EventHistory)
  }
}
