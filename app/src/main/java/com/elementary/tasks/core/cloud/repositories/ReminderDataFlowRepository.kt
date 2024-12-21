package com.elementary.tasks.core.cloud.repositories

import com.github.naz013.domain.Reminder
import com.github.naz013.repository.ReminderRepository

class ReminderDataFlowRepository(
  private val reminderRepository: ReminderRepository
) : DatabaseRepository<Reminder>() {
  override suspend fun get(id: String): Reminder? {
    return reminderRepository.getById(id)
  }

  override suspend fun insert(t: Reminder) {
    reminderRepository.save(t)
  }

  override suspend fun all(): List<Reminder> {
    return reminderRepository.getAll()
  }

  override suspend fun delete(t: Reminder) {
    reminderRepository.delete(t.uuId)
  }
}
