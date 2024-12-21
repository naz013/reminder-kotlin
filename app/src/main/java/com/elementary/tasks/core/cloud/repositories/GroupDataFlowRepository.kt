package com.elementary.tasks.core.cloud.repositories

import com.github.naz013.domain.ReminderGroup
import com.github.naz013.repository.ReminderGroupRepository

class GroupDataFlowRepository(
  private val reminderGroupRepository: ReminderGroupRepository
) : DatabaseRepository<ReminderGroup>() {
  override suspend fun get(id: String): ReminderGroup? {
    return reminderGroupRepository.getById(id)
  }

  override suspend fun insert(t: ReminderGroup) {
    reminderGroupRepository.save(t)
  }

  override suspend fun all(): List<ReminderGroup> {
    return reminderGroupRepository.getAll()
  }

  override suspend fun delete(t: ReminderGroup) {
    reminderGroupRepository.delete(t.groupUuId)
  }
}
