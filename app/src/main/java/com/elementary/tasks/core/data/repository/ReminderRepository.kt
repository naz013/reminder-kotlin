package com.elementary.tasks.core.data.repository

import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.ui.reminder.UiReminderType

class ReminderRepository(private val reminderDao: ReminderDao) {

  fun getById(id: String): Reminder? {
    return reminderDao.getById(id)
  }

  fun getActive(): List<Reminder> {
    return reminderDao.getAll(active = true, removed = false)
  }

  fun getActiveWithoutGpsTypes(): List<Reminder> {
    return reminderDao.getAll(active = true, removed = false).filterNot {
      UiReminderType(it.type).isGpsType()
    }
  }

  suspend fun getActiveGpsTypes(): List<Reminder> {
    return reminderDao.getAllTypes(active = true, removed = false, types = Reminder.gpsTypes())
  }

  suspend fun save(reminder: Reminder) {
    reminderDao.insert(reminder)
  }
}
