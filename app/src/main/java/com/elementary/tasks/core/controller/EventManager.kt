package com.elementary.tasks.core.controller

import com.elementary.tasks.core.appwidgets.UpdatesHelper
import com.elementary.tasks.core.data.invokeSuspend
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.domain.Reminder
import com.github.naz013.repository.ReminderRepository

abstract class EventManager(
  protected val reminder: Reminder,
  private val reminderRepository: ReminderRepository,
  protected val prefs: Prefs,
  protected val notifier: Notifier,
  protected val updatesHelper: UpdatesHelper
) : EventControl {

  protected fun save() {
    invokeSuspend { reminderRepository.save(reminder) }
    updatesHelper.updateWidgets()
    if (prefs.isSbNotificationEnabled) {
      notifier.sendShowReminderPermanent()
    }
  }

  protected fun remove() {
    invokeSuspend { reminderRepository.delete(reminder.uuId) }
    updatesHelper.updateWidgets()
    if (prefs.isSbNotificationEnabled) {
      notifier.sendShowReminderPermanent()
    }
  }
}
