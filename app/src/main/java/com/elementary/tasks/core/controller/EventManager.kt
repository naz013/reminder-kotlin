package com.elementary.tasks.core.controller

import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.params.Prefs

abstract class EventManager(
  val reminder: Reminder,
  protected val db: AppDb,
  protected val prefs: Prefs,
  protected val notifier: Notifier,
  protected val jobScheduler: JobScheduler,
  protected val updatesHelper: UpdatesHelper
) : EventControl {

  protected fun save() {
    db.reminderDao().insert(reminder)
    updatesHelper.updateWidgets()
    if (prefs.isSbNotificationEnabled) {
      notifier.sendShowReminderPermanent()
    }
  }

  protected fun remove() {
    db.reminderDao().delete(reminder)
    updatesHelper.updateWidgets()
    if (prefs.isSbNotificationEnabled) {
      notifier.sendShowReminderPermanent()
    }
  }
}
