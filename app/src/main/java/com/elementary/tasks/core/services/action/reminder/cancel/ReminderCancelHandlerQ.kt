package com.elementary.tasks.core.services.action.reminder.cancel

import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.action.ActionHandler
import com.elementary.tasks.core.utils.Notifier

class ReminderCancelHandlerQ(
  private val notifier: Notifier,
  private val eventControlFactory: EventControlFactory
) : ActionHandler<Reminder> {

  override fun handle(data: Reminder) {
    eventControlFactory.getController(data).next()
    notifier.cancel(data.uniqueId)
  }
}
