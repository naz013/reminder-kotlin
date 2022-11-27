package com.elementary.tasks.core.view_models.reminders

import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.WorkManagerProvider
import com.elementary.tasks.core.view_models.DispatcherProvider

class ActiveRemindersViewModel(
  appDb: AppDb,
  prefs: Prefs,
  calendarUtils: CalendarUtils,
  eventControlFactory: EventControlFactory,
  dispatcherProvider: DispatcherProvider,
  workManagerProvider: WorkManagerProvider
) : BaseRemindersViewModel(
  appDb,
  prefs,
  calendarUtils,
  eventControlFactory,
  dispatcherProvider,
  workManagerProvider
) {
  val events = appDb.reminderDao().loadNotRemoved(removed = false)
}
