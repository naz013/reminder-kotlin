package com.elementary.tasks.core.services.action.reminder

import android.os.Build
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.services.action.ActionHandler
import com.elementary.tasks.core.services.action.WearNotification
import com.elementary.tasks.core.services.action.reminder.cancel.ReminderCancelHandler
import com.elementary.tasks.core.services.action.reminder.cancel.ReminderCancelHandler29
import com.elementary.tasks.core.services.action.reminder.process.ReminderHandler
import com.elementary.tasks.core.services.action.reminder.process.ReminderHandler29
import com.elementary.tasks.core.services.action.reminder.process.ReminderHandlerQuiet
import com.elementary.tasks.core.services.action.reminder.snooze.ReminderSnoozeHandler
import com.elementary.tasks.core.services.action.reminder.snooze.ReminderSnoozeHandler29
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
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ReminderHandler29(
          reminderDataProvider,
          contextProvider,
          textProvider,
          notifier,
          prefs,
          wearNotification
        )
      } else {
        ReminderHandler(contextProvider, notifier)
      }
    } else {
      ReminderHandlerQuiet(
        reminderDataProvider,
        contextProvider,
        textProvider,
        notifier,
        prefs,
        wearNotification
      )
    }
  }

  fun createCancel(): ActionHandler<Reminder> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      ReminderCancelHandler29(notifier, eventControlFactory)
    } else {
      ReminderCancelHandler(notifier, eventControlFactory, contextProvider)
    }
  }

  fun createSnooze(): ActionHandler<Reminder> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      ReminderSnoozeHandler29(notifier, eventControlFactory, prefs)
    } else {
      ReminderSnoozeHandler(notifier, eventControlFactory, prefs)
    }
  }
}
