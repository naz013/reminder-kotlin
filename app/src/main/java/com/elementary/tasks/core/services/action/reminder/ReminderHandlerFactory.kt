package com.elementary.tasks.core.services.action.reminder

import com.elementary.tasks.core.controller.EventControlFactory
import com.github.naz013.domain.Reminder
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.services.action.ActionHandler
import com.elementary.tasks.core.services.action.WearNotification
import com.elementary.tasks.core.services.action.reminder.cancel.ReminderCancelHandlerQ
import com.elementary.tasks.core.services.action.reminder.process.ReminderHandlerQ
import com.elementary.tasks.core.services.action.reminder.process.ReminderHandlerSilent
import com.elementary.tasks.core.services.action.reminder.snooze.ReminderSnoozeHandlerQ
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.params.Prefs

class ReminderHandlerFactory(
  private val reminderDataProvider: ReminderDataProvider,
  private val contextProvider: ContextProvider,
  private val textProvider: TextProvider,
  private val notifier: Notifier,
  private val eventControlFactory: EventControlFactory,
  private val prefs: Prefs,
  private val wearNotification: WearNotification
) {

  fun createAction(canShowWindow: Boolean): ActionHandler<Reminder> {
    return if (canShowWindow) {
      ReminderHandlerQ(
        reminderDataProvider = reminderDataProvider,
        contextProvider = contextProvider,
        textProvider = textProvider,
        notifier = notifier,
        prefs = prefs,
        wearNotification = wearNotification
      )
    } else {
      ReminderHandlerSilent(
        reminderDataProvider = reminderDataProvider,
        contextProvider = contextProvider,
        textProvider = textProvider,
        notifier = notifier,
        prefs = prefs,
        wearNotification = wearNotification
      )
    }
  }

  fun createCancel(): ActionHandler<Reminder> {
    return ReminderCancelHandlerQ(
      notifier = notifier,
      eventControlFactory = eventControlFactory
    )
  }

  fun createSnooze(): ActionHandler<Reminder> {
    return ReminderSnoozeHandlerQ(
      notifier = notifier,
      eventControlFactory = eventControlFactory,
      prefs = prefs
    )
  }
}
