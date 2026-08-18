package com.github.naz013.repository

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.LocalDateTime

interface ReminderV2Repository {
  suspend fun save(reminder: ReminderV2)

  suspend fun getById(id: String): ReminderV2?
  suspend fun getAll(): List<ReminderV2>
  suspend fun getAll(active: Boolean, removed: Boolean): List<ReminderV2>
  suspend fun getByRemovedStatus(removed: Boolean): List<ReminderV2>
  suspend fun getActiveInRange(
    removed: Boolean,
    from: LocalDateTime,
    to: LocalDateTime
  ): List<ReminderV2>
  fun observeActiveInRange(
    removed: Boolean,
    from: LocalDateTime,
    to: LocalDateTime
  ): Flow<List<ReminderV2>>
  suspend fun getByGroupId(groupId: String): List<ReminderV2>
  suspend fun countActiveByGroupId(groupId: String): Int
  suspend fun clearGroupId(groupId: String)
  suspend fun getByNoteId(noteId: String): List<ReminderV2>
  suspend fun search(query: String): List<ReminderV2>

  suspend fun delete(id: String)
  suspend fun deleteAll(ids: List<String>)
  suspend fun deleteAll()

  suspend fun getIdsByState(syncStates: List<SyncState>): List<String>
  suspend fun updateSyncState(id: String, state: SyncState)
  suspend fun getAllIds(): List<String>
}
