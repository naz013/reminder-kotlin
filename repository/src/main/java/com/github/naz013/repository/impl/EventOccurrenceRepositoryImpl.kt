package com.github.naz013.repository.impl

import com.github.naz013.domain.occurance.EventOccurrence
import com.github.naz013.repository.EventOccurrenceRepository
import com.github.naz013.repository.dao.EventOccurrenceDao
import com.github.naz013.repository.entity.EventOccurrenceEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

internal class EventOccurrenceRepositoryImpl(
  private val dao: EventOccurrenceDao,
  private val tableChangeNotifier: TableChangeNotifier
) : EventOccurrenceRepository {

  override suspend fun save(occurrence: EventOccurrence) {
    dao.insert(EventOccurrenceEntity(occurrence))
    tableChangeNotifier.notify(Table.EventOccurrence)
  }

  override suspend fun saveAll(occurrences: List<EventOccurrence>) {
    val entities = occurrences.map { EventOccurrenceEntity(it) }
    dao.insertAll(entities)
    tableChangeNotifier.notify(Table.EventOccurrence)
  }

  override suspend fun getByDateRange(
    startDate: LocalDate,
    endDate: LocalDate
  ): List<EventOccurrence> {
    return dao.getByDateRange(startDate.toEpochDay(), endDate.toEpochDay()).map { it.toDomain() }
  }

  override suspend fun getByEventId(eventId: String): List<EventOccurrence> {
    return dao.getByEventId(eventId).map { it.toDomain() }
  }

  override suspend fun getByDateAndTimeRange(
    date: LocalDate,
    startTime: LocalTime,
    endTime: LocalTime
  ): List<EventOccurrence> {
    return dao.getByDateAndTimeRange(
      date.toEpochDay(),
      startTime.toSecondOfDay(),
      endTime.toSecondOfDay()
    ).map { it.toDomain() }
  }

  override suspend fun deleteById(id: String) {
    dao.deleteById(id)
    tableChangeNotifier.notify(Table.EventOccurrence)
  }

  override suspend fun deleteByEventId(eventId: String) {
    dao.deleteByEventId(eventId)
    tableChangeNotifier.notify(Table.EventOccurrence)
  }

  override suspend fun deleteAll() {
    dao.deleteAll()
    tableChangeNotifier.notify(Table.EventOccurrence)
  }
}
