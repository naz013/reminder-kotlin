package com.elementary.tasks.core.controller

import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.params.Prefs

abstract class EventManager(
  protected val reminder: Reminder,
  private val reminderDao: ReminderDao,
  protected val prefs: Prefs,
  protected val notifier: Notifier,
  protected val updatesHelper: UpdatesHelper
) : EventControl {

  protected fun save() {
    reminderDao.insert(reminder)
    updatesHelper.updateWidgets()
    if (prefs.isSbNotificationEnabled) {
      notifier.sendShowReminderPermanent()
    }
  }

  protected fun remove() {
    reminderDao.delete(reminder)
    updatesHelper.updateWidgets()
    if (prefs.isSbNotificationEnabled) {
      notifier.sendShowReminderPermanent()
    }
  }
}
