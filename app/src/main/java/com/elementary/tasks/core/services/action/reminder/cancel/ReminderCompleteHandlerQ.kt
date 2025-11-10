package com.elementary.tasks.core.services.action.reminder.cancel

import com.elementary.tasks.core.services.action.ActionHandler
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.reminder.scheduling.usecase.CompleteReminderUseCase
import com.github.naz013.domain.Reminder

class ReminderCompleteHandlerQ(
  private val notifier: Notifier,
  private val completeReminderUseCase: CompleteReminderUseCase
) : ActionHandler<Reminder> {

  override suspend fun handle(data: Reminder) {
    completeReminderUseCase(data)
    notifier.cancel(data.uniqueId)
  }
}
