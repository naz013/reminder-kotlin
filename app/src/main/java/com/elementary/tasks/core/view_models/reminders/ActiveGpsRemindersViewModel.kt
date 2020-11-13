package com.elementary.tasks.core.view_models.reminders

import com.elementary.tasks.core.data.models.Reminder

class ActiveGpsRemindersViewModel : BaseRemindersViewModel() {

  val events = appDb.reminderDao().loadAllTypes(
    active = true,
    removed = false,
    types = Reminder.gpsTypes()
  )
}
