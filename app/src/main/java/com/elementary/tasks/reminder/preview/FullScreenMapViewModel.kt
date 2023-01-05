package com.elementary.tasks.reminder.preview

import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.utils.DispatcherProvider

class FullScreenMapViewModel(
  id: String,
  reminderDao: ReminderDao,
  dispatcherProvider: DispatcherProvider
) : BaseProgressViewModel(dispatcherProvider) {
  val reminder = reminderDao.loadById(id)
}
