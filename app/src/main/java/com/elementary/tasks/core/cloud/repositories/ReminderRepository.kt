package com.elementary.tasks.core.cloud.repositories

import com.elementary.tasks.core.data.models.Reminder

class ReminderRepository : DatabaseRepository<Reminder>() {
    override fun get(id: String): Reminder? {
        return appDb.reminderDao().getById(id)
    }

    override fun insert(t: Reminder) {
        appDb.reminderDao().insert(t)
    }
}