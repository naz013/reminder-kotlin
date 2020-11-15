package com.elementary.tasks.core.view_models.reminders

import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Prefs

class ActiveGpsRemindersViewModel(
  appDb: AppDb,
  prefs: Prefs,
  calendarUtils: CalendarUtils,
  eventControlFactory: EventControlFactory
) : BaseRemindersViewModel(appDb, prefs, calendarUtils, eventControlFactory) {

  val events = appDb.reminderDao().loadAllTypes(
    active = true,
    removed = false,
    types = Reminder.gpsTypes()
  )
}
