package com.github.naz013.repository.impl

import com.github.naz013.domain.GoogleCalendarEvent
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GoogleCalendarEventRepository
import com.github.naz013.repository.dao.GoogleCalendarEventDao
import com.github.naz013.repository.entity.GoogleCalendarEventEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table

internal class GoogleCalendarEventRepositoryImpl(
  private val dao: GoogleCalendarEventDao,
  private val tableChangeNotifier: TableChangeNotifier
) : GoogleCalendarEventRepository {

  private val table = Table.GoogleCalendarEvent

  override suspend fun save(event: GoogleCalendarEvent) {
    Logger.d(TAG, "Save Google Calendar event: ${event.uuId}")
    dao.insert(GoogleCalendarEventEntity(event))
    tableChangeNotifier.notify(table)
  }

  override suspend fun getById(id: String): GoogleCalendarEvent? {
    return dao.getByKey(id)?.toDomain()
  }

  override suspend fun getByDeviceEventId(deviceEventId: Long): GoogleCalendarEvent? {
    return dao.getByDeviceEventId(deviceEventId)?.toDomain()
  }

  override suspend fun getVisible(): List<GoogleCalendarEvent> {
    return dao.visible().map { it.toDomain() }
  }

  override suspend fun knownDeviceEventIds(): List<Long> {
    return dao.knownDeviceEventIds()
  }

  override suspend fun markDismissed(id: String) {
    Logger.d(TAG, "Mark Google Calendar event dismissed: $id")
    dao.markDismissed(id)
    tableChangeNotifier.notify(table)
  }

  override suspend fun deleteByDeviceEventId(deviceEventId: Long) {
    Logger.d(TAG, "Delete Google Calendar event by device event id: $deviceEventId")
    dao.deleteByDeviceEventId(deviceEventId)
    tableChangeNotifier.notify(table)
  }

  override suspend fun deleteAll() {
    Logger.d(TAG, "Delete all Google Calendar events")
    dao.deleteAll()
    tableChangeNotifier.notify(table)
  }

  companion object {
    private const val TAG = "GoogleCalendarEventRepository"
  }
}
