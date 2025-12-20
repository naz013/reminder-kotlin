package com.elementary.tasks.core.services.action.reminder

import com.elementary.tasks.core.services.action.ActionHandler
import com.elementary.tasks.core.services.action.WearNotification
import com.elementary.tasks.core.services.action.reminder.cancel.ReminderCompleteHandlerQ
import com.elementary.tasks.core.services.action.reminder.process.ReminderHandlerQ
import com.elementary.tasks.core.services.action.reminder.process.ReminderHandlerSilent
import com.elementary.tasks.core.services.action.reminder.snooze.ReminderSnoozeHandlerQ
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.reminder.scheduling.usecase.CompleteReminderUseCase
import com.elementary.tasks.reminder.scheduling.usecase.SnoozeReminderUseCase
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.Reminder

class ReminderHandlerFactory(
  private val reminderDataProvider: ReminderDataProvider,
  private val contextProvider: ContextProvider,
  private val textProvider: TextProvider,
  private val notifier: Notifier,
  private val prefs: Prefs,
  private val wearNotification: WearNotification,
  private val completeReminderUseCase: CompleteReminderUseCase,
  private val snoozeReminderUseCase: SnoozeReminderUseCase
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

  fun createComplete(): ActionHandler<Reminder> {
    return ReminderCompleteHandlerQ(
      notifier = notifier,
      completeReminderUseCase = completeReminderUseCase
    )
  }

  fun createSnooze(): ActionHandler<Reminder> {
    return ReminderSnoozeHandlerQ(
      notifier = notifier,
      prefs = prefs,
      snoozeReminderUseCase = snoozeReminderUseCase
    )
  }
}
