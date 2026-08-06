package com.github.naz013.repository.impl

import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger
import com.github.naz013.repository.dao.ReminderDao
import com.github.naz013.repository.migration.ReminderRepository

@Deprecated("Use ReminderV2")
internal class ReminderRepositoryImpl(
  private val dao: ReminderDao,
) : ReminderRepository {

  override suspend fun getAll(): List<Reminder> {
    Logger.d(TAG, "Get all reminders")
    return dao.getAll().map { it.toDomain() }
  }

  companion object {
    private const val TAG = "ReminderRepository"
  }
}
