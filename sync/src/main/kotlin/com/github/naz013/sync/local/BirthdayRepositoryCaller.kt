package com.github.naz013.sync.local

import com.github.naz013.domain.Birthday
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.BirthdayRepository

internal class BirthdayRepositoryCaller(
  private val birthdayRepository: BirthdayRepository
) : DataTypeRepositoryCaller<Birthday> {

  override suspend fun getById(id: String): Birthday? {
    return birthdayRepository.getById(id)
  }

  override suspend fun getIdsByState(states: List<SyncState>): List<String> {
    return birthdayRepository.getIdsByState(states)
  }

  override suspend fun updateSyncState(
    id: String,
    state: SyncState
  ) {
    birthdayRepository.updateSyncState(id, state)
  }

  override suspend fun insertOrUpdate(item: Birthday) {
    birthdayRepository.save(item)
  }
}
