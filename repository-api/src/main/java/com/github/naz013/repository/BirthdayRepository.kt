package com.github.naz013.repository

import com.github.naz013.domain.Birthday
import com.github.naz013.domain.sync.SyncState

interface BirthdayRepository {
  suspend fun save(birthday: Birthday)
  suspend fun updateSyncState(id: String, state: SyncState)

  suspend fun getById(id: String): Birthday?
  suspend fun getByDayMonth(day: Int, month: Int): List<Birthday>
  suspend fun getIdsByState(syncStates: List<SyncState>): List<String>
  suspend fun searchByName(query: String): List<Birthday>

  suspend fun getAll(): List<Birthday>
  suspend fun getAll(dayMonth: String): List<Birthday>

  suspend fun delete(id: String)
  suspend fun deleteAll()

  suspend fun getAllIds(): List<String>
}
