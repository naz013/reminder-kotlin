package com.elementary.tasks.core.view_models.reminders

import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.view_models.BaseProgressViewModel
import com.elementary.tasks.core.view_models.DispatcherProvider

class FullScreenMapViewModel(
  id: String,
  reminderDao: ReminderDao,
  dispatcherProvider: DispatcherProvider
) : BaseProgressViewModel(dispatcherProvider) {
  val reminder = reminderDao.loadById(id)
}
