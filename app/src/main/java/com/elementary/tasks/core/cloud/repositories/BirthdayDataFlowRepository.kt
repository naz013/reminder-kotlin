package com.elementary.tasks.core.cloud.repositories

import com.github.naz013.domain.Birthday
import com.github.naz013.repository.BirthdayRepository

class BirthdayDataFlowRepository(
  private val birthdayRepository: BirthdayRepository
) : DatabaseRepository<Birthday>() {
  override suspend fun get(id: String): Birthday? {
    return birthdayRepository.getById(id)
  }

  override suspend fun insert(t: Birthday) {
    birthdayRepository.save(t)
  }

  override suspend fun all(): List<Birthday> {
    return birthdayRepository.getAll()
  }

  override suspend fun delete(t: Birthday) {
    birthdayRepository.delete(t.uuId)
  }
}
