package com.elementary.tasks.core.cloud.repositories

import com.elementary.tasks.core.data.models.ReminderGroup

class GroupRepository : DatabaseRepository<ReminderGroup>() {
    override suspend fun get(id: String): ReminderGroup? {
        return appDb.reminderGroupDao().getById(id)
    }

    override suspend fun insert(t: ReminderGroup) {
        appDb.reminderGroupDao().insert(t)
    }

    override suspend fun all(): List<ReminderGroup> {
        return appDb.reminderGroupDao().all()
    }

    override suspend fun delete(t: ReminderGroup) {
        appDb.reminderGroupDao().delete(t)
    }
}