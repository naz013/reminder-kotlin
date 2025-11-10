package com.elementary.tasks.core.services.action.reminder.snooze

import com.elementary.tasks.core.services.action.ActionHandler
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.reminder.scheduling.usecase.SnoozeReminderUseCase
import com.github.naz013.domain.Reminder

class ReminderSnoozeHandlerQ(
  private val notifier: Notifier,
  private val prefs: Prefs,
  private val snoozeReminderUseCase: SnoozeReminderUseCase
) : ActionHandler<Reminder> {

  override suspend fun handle(data: Reminder) {
    snoozeReminderUseCase(reminder = data, timeInMinutes = prefs.snoozeTime)
    notifier.cancel(data.uniqueId)
  }
}
