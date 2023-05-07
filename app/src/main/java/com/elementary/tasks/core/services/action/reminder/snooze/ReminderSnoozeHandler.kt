package com.elementary.tasks.core.services.action.reminder.snooze

import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.action.ActionHandler
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.params.Prefs

@Deprecated("After Q")
class ReminderSnoozeHandler(
  private val notifier: Notifier,
  private val eventControlFactory: EventControlFactory,
  private val prefs: Prefs
) : ActionHandler<Reminder> {

  override fun handle(data: Reminder) {
    eventControlFactory.getController(data).setDelay(prefs.snoozeTime)
    notifier.cancel(data.uniqueId)
  }
}
