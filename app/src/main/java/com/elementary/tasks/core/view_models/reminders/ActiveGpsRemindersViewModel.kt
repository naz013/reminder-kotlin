package com.elementary.tasks.core.view_models.reminders

import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.view_models.BaseProgressViewModel
import com.elementary.tasks.core.view_models.DispatcherProvider

class ActiveGpsRemindersViewModel(
  reminderDao: ReminderDao,
  dispatcherProvider: DispatcherProvider
) : BaseProgressViewModel(dispatcherProvider) {

  val events = reminderDao.loadAllTypes(
    active = true,
    removed = false,
    types = Reminder.gpsTypes()
  )
}
