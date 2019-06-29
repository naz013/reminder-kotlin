package com.elementary.tasks.core.cloud.repositories

import com.elementary.tasks.core.data.models.Birthday

class BirthdayRepository : DatabaseRepository<Birthday>() {
    override suspend fun get(id: String): Birthday? {
        return appDb.birthdaysDao().getById(id)
    }

    override suspend fun insert(t: Birthday) {
        appDb.birthdaysDao().insert(t)
    }
}