package com.elementary.tasks.core.cloud.repositories

import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Reminder

class ReminderDataFlowRepository(appDb: AppDb) : DatabaseRepository<Reminder>(appDb) {
  override suspend fun get(id: String): Reminder? {
    return appDb.reminderDao().getById(id)
  }

  override suspend fun insert(t: Reminder) {
    appDb.reminderDao().insert(t)
  }

  override suspend fun all(): List<Reminder> {
    return appDb.reminderDao().getAll()
  }

  override suspend fun delete(t: Reminder) {
    appDb.reminderDao().delete(t)
  }
}
