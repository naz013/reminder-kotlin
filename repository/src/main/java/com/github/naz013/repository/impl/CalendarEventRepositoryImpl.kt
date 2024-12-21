package com.github.naz013.repository.impl

import com.github.naz013.domain.CalendarEvent
import com.github.naz013.logging.Logger
import com.github.naz013.repository.CalendarEventRepository
import com.github.naz013.repository.dao.CalendarEventsDao
import com.github.naz013.repository.entity.CalendarEventEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table

internal class CalendarEventRepositoryImpl(
  private val dao: CalendarEventsDao,
  private val tableChangeNotifier: TableChangeNotifier
) : CalendarEventRepository {

  private val table = Table.CalendarEvent

  override suspend fun save(calendarEvent: CalendarEvent) {
    Logger.d(TAG, "Save calendar event: ${calendarEvent.uuId}")
    dao.insert(CalendarEventEntity(calendarEvent))
    tableChangeNotifier.notify(table)
  }

  override suspend fun getAll(): List<CalendarEvent> {
    Logger.d(TAG, "Get all calendar events")
    return dao.all().map { it.toDomain() }
  }

  override suspend fun getByReminderId(id: String): List<CalendarEvent> {
    Logger.d(TAG, "Get calendar events by reminder id: $id")
    return dao.getByReminder(id).map { it.toDomain() }
  }

  override suspend fun eventIds(): List<Long> {
    Logger.d(TAG, "Get all calendar event ids")
    return dao.eventIds()
  }

  override suspend fun delete(id: String) {
    Logger.d(TAG, "Delete calendar event: $id")
    dao.deleteById(id)
    tableChangeNotifier.notify(table)
  }

  companion object {
    private const val TAG = "CalendarEventRepository"
  }
}
