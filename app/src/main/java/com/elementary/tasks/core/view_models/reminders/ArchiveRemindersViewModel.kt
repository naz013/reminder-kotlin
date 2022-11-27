package com.elementary.tasks.core.view_models.reminders

import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.WorkManagerProvider
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.reminder.work.ReminderDeleteBackupWorker

class ArchiveRemindersViewModel(
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

  val events = appDb.reminderDao().loadNotRemoved(removed = true)

  fun deleteAll(data: List<Reminder>) {
    postInProgress(true)
    launchDefault {
      data.forEach {
        eventControlFactory.getController(it).stop()
      }
      appDb.reminderDao().deleteAll(data)
      data.forEach {
        startWork(ReminderDeleteBackupWorker::class.java, Constants.INTENT_ID, it.uuId)
      }
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }
}
