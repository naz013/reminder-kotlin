package com.elementary.tasks.core.cloud.repositories

import com.elementary.tasks.core.data.models.Reminder

class ReminderRepository : DatabaseRepository<Reminder>() {
    override suspend fun get(id: String): Reminder? {
        return appDb.reminderDao().getById(id)
    }

    override suspend fun insert(t: Reminder) {
        appDb.reminderDao().insert(t)
    }
}