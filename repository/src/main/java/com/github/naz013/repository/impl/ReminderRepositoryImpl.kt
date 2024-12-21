package com.github.naz013.repository.impl

import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.repository.dao.ReminderDao
import com.github.naz013.repository.entity.ReminderEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table

internal class ReminderRepositoryImpl(
  private val dao: ReminderDao,
  private val tableChangeNotifier: TableChangeNotifier
) : ReminderRepository {

  private val table = Table.Reminder

  override suspend fun getById(id: String): Reminder? {
    Logger.d(TAG, "Get reminder by id: $id")
    return dao.getById(id)?.toDomain()
  }

  override suspend fun getActive(): List<Reminder> {
    Logger.d(TAG, "Get active reminders")
    return dao.getAll(active = true, removed = false).map { it.toDomain() }
  }

  override suspend fun getActiveWithoutGpsTypes(): List<Reminder> {
    Logger.d(TAG, "Get active reminders without gps types")
    return dao.getAll(
      active = true,
      removed = false
    ).filterNot {
      Reminder.gpsTypes().contains(it.type)
    }.map { it.toDomain() }
  }

  override suspend fun getActiveGpsTypes(): List<Reminder> {
    Logger.d(TAG, "Get active reminders with gps types")
    return dao.getAllTypes(active = true, removed = false, types = Reminder.gpsTypes())
      .map { it.toDomain() }
  }

  override suspend fun getAll(active: Boolean, removed: Boolean): List<Reminder> {
    Logger.d(TAG, "Get all reminders, active: $active, removed: $removed")
    return dao.getAll(active, removed).map { it.toDomain() }
  }

  override suspend fun getAll(): List<Reminder> {
    Logger.d(TAG, "Get all reminders")
    return dao.getAll().map { it.toDomain() }
  }

  override suspend fun getAllTypes(
    active: Boolean,
    removed: Boolean,
    types: IntArray
  ): List<Reminder> {
    Logger.d(TAG, "Get all reminders by types, active: $active, removed: $removed, types: $types")
    return dao.getAllTypes(active, removed, types).map { it.toDomain() }
  }

  override suspend fun getByNoteKey(key: String): List<Reminder> {
    Logger.d(TAG, "Get reminders by note key: $key")
    return dao.getByNoteKey(key).map { it.toDomain() }
  }

  override suspend fun getByRemovedStatus(removed: Boolean): List<Reminder> {
    Logger.d(TAG, "Get reminders by removed status: $removed")
    return dao.getByRemovedStatus(removed).map { it.toDomain() }
  }

  override suspend fun getActiveInRange(
    removed: Boolean,
    fromTime: String,
    toTime: String
  ): List<Reminder> {
    Logger.d(TAG, "Get active reminders in range, removed: $removed, from: $fromTime, to: $toTime")
    return dao.getActiveInRange(removed, fromTime, toTime).map { it.toDomain() }
  }

  override suspend fun searchBySummaryAndRemovedStatus(
    query: String,
    removed: Boolean
  ): List<Reminder> {
    Logger.d(
      TAG,
      "Search reminders by summary and removed status, query: $query, removed: $removed"
    )
    return dao.searchBySummaryAndRemovedStatus(query, removed).map { it.toDomain() }
  }

  override suspend fun getAllTypesInRange(
    active: Boolean,
    removed: Boolean,
    fromTime: String,
    toTime: String
  ): List<Reminder> {
    val logMessage = "Get all reminders by types in range, active: $active, " +
      "removed: $removed, from: $fromTime, to: $toTime"
    Logger.d(TAG, logMessage)
    return dao.getAllTypesInRange(active, removed, fromTime, toTime).map { it.toDomain() }
  }

  override suspend fun searchBySummaryAllTypes(
    query: String,
    active: Boolean,
    removed: Boolean,
    types: IntArray
  ): List<Reminder> {
    val logMessage = "Search reminders by summary and all types, query: $query, " +
      "active: $active, removed: $removed, types: $types"
    Logger.d(TAG, logMessage)
    return dao.searchBySummaryAllTypes(query, active, removed, types).map { it.toDomain() }
  }

  override suspend fun search(query: String): List<Reminder> {
    Logger.d(TAG, "Search reminders by query: $query")
    return dao.search(query).map { it.toDomain() }
  }

  override suspend fun save(reminder: Reminder) {
    Logger.d(TAG, "Save reminder: ${reminder.uuId}")
    dao.insert(ReminderEntity(reminder))
    tableChangeNotifier.notify(table)
  }

  override suspend fun delete(id: String) {
    Logger.d(TAG, "Delete reminder by id: $id")
    dao.delete(id)
    tableChangeNotifier.notify(table)
  }

  override suspend fun deleteAll() {
    Logger.d(TAG, "Delete all reminders")
    dao.deleteAll()
    tableChangeNotifier.notify(table)
  }

  override suspend fun deleteAll(ids: List<String>) {
    Logger.d(TAG, "Delete all reminders by ids: $ids")
    dao.deleteAll(ids)
    tableChangeNotifier.notify(table)
  }

  companion object {
    private const val TAG = "ReminderRepository"
  }
}
