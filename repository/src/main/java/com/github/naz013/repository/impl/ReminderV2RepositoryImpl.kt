package com.github.naz013.repository.impl

import com.github.naz013.domain.Reminder
import com.github.naz013.domain.reminder.migration.toReminder
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.dao.ReminderDao
import com.github.naz013.repository.dao.ReminderV2Dao
import com.github.naz013.repository.entity.ReminderEntity
import com.github.naz013.repository.entity.toDomain
import com.github.naz013.repository.entity.toEntity
import com.github.naz013.repository.entity.toEpochMillisUtc
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table
import org.threeten.bp.LocalDateTime

/**
 * [reminderDao] is mirrored on every write so the V1 `Reminder` table never goes stale for its
 * remaining readers, now that this repository is the write-of-record (see the ReminderV2/GroupV2
 * linking plan, Phase C5). Mirrors via the raw DAO, not [com.github.naz013.repository.ReminderRepository],
 * so this doesn't re-trigger that repository's own (unrelated, still-active) V1-to-V2 forward
 * mirror and bounce back here.
 */
internal class ReminderV2RepositoryImpl(
  private val dao: ReminderV2Dao,
  private val tableChangeNotifier: TableChangeNotifier,
  private val reminderDao: ReminderDao
) : ReminderV2Repository {

  private val table = Table.ReminderV2

  override suspend fun save(reminder: ReminderV2) {
    Logger.d(TAG, "Save reminder: ${reminder.uuId}")
    dao.insert(reminder.toEntity())
    tableChangeNotifier.notify(table)
    reminderDao.insert(ReminderEntity(reminder.toReminder().apply { jsonSchemaVersion = Reminder.Version.V3 }))
  }

  override suspend fun getById(id: String): ReminderV2? {
    Logger.d(TAG, "Get reminder by id: $id")
    return dao.getById(id)?.toDomain()
  }

  override suspend fun getAll(): List<ReminderV2> {
    Logger.d(TAG, "Get all reminders")
    return dao.getAll().map { it.toDomain() }
  }

  override suspend fun getAll(active: Boolean, removed: Boolean): List<ReminderV2> {
    Logger.d(TAG, "Get all reminders, active: $active, removed: $removed")
    return dao.getAll(active, removed).map { it.toDomain() }
  }

  override suspend fun getByRemovedStatus(removed: Boolean): List<ReminderV2> {
    Logger.d(TAG, "Get reminders by removed status: $removed")
    return dao.getByRemovedStatus(removed).map { it.toDomain() }
  }

  override suspend fun getActiveInRange(
    removed: Boolean,
    from: LocalDateTime,
    to: LocalDateTime
  ): List<ReminderV2> {
    Logger.d(TAG, "Get active reminders in range, removed: $removed, from: $from, to: $to")
    return dao.getActiveInRange(
      removed = removed,
      active = true,
      fromMillis = from.toEpochMillisUtc(),
      toMillis = to.toEpochMillisUtc()
    ).map { it.toDomain() }
  }

  override suspend fun getByGroupId(groupId: String): List<ReminderV2> {
    Logger.d(TAG, "Get reminders by group id: $groupId")
    return dao.getByGroupId(groupId).map { it.toDomain() }
  }

  override suspend fun countActiveByGroupId(groupId: String): Int {
    Logger.d(TAG, "Count active reminders by group id: $groupId")
    return dao.countActiveByGroupId(groupId)
  }

  override suspend fun getByNoteId(noteId: String): List<ReminderV2> {
    Logger.d(TAG, "Get reminders by note id: $noteId")
    return dao.getByNoteId(noteId).map { it.toDomain() }
  }

  override suspend fun search(query: String): List<ReminderV2> {
    Logger.d(TAG, "Search reminders by query: $query")
    return dao.search(query).map { it.toDomain() }
  }

  override suspend fun delete(id: String) {
    Logger.d(TAG, "Delete reminder by id: $id")
    dao.delete(id)
    tableChangeNotifier.notify(table)
    reminderDao.delete(id)
  }

  override suspend fun deleteAll(ids: List<String>) {
    Logger.d(TAG, "Delete all reminders by ids: $ids")
    dao.deleteAll(ids)
    tableChangeNotifier.notify(table)
    reminderDao.deleteAll(ids)
  }

  override suspend fun deleteAll() {
    Logger.d(TAG, "Delete all reminders")
    dao.deleteAll()
    tableChangeNotifier.notify(table)
    reminderDao.deleteAll()
  }

  override suspend fun getIdsByState(syncStates: List<SyncState>): List<String> {
    Logger.d(TAG, "Get reminder ids by sync states: $syncStates")
    return dao.getBySyncStates(syncStates.map { it.name })
  }

  override suspend fun updateSyncState(id: String, state: SyncState) {
    Logger.d(TAG, "Update reminder sync state, id: $id, state: $state")
    dao.updateSyncState(id, state.name)
    tableChangeNotifier.notify(table)
    reminderDao.updateSyncState(id, state.name)
  }

  override suspend fun getAllIds(): List<String> {
    Logger.d(TAG, "Get all reminder ids")
    return dao.getAllIds()
  }

  companion object {
    private const val TAG = "ReminderV2Repository"
  }
}
