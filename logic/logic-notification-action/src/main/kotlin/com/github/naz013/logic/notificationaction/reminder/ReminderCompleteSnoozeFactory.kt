package com.github.naz013.logic.notificationaction.reminder

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.notificationaction.ActionHandler
import com.github.naz013.logic.notificationaction.CancelNotificationDecorator
import com.github.naz013.logic.notificationaction.NotificationGateway
import com.github.naz013.logic.reminder.ReminderPreferences
import com.github.naz013.logic.reminder.usecase.CompleteReminderUseCase
import com.github.naz013.logic.reminder.usecase.SnoozeReminderUseCase

class ReminderCompleteSnoozeFactory(
  private val notificationGateway: NotificationGateway,
  private val reminderPreferences: ReminderPreferences,
  private val completeReminderUseCase: CompleteReminderUseCase,
  private val snoozeReminderUseCase: SnoozeReminderUseCase,
) {
  fun createComplete(): ActionHandler<ReminderV2> =
    CancelNotificationDecorator(
      delegate = ActionHandler { reminder: ReminderV2 -> completeReminderUseCase(reminder) },
      notificationGateway = notificationGateway,
      uniqueId = ReminderV2::uniqueId,
    )

  fun createSnooze(): ActionHandler<ReminderV2> =
    CancelNotificationDecorator(
      delegate =
      ActionHandler { reminder: ReminderV2 ->
        snoozeReminderUseCase(reminder = reminder, timeInMinutes = reminderPreferences.snoozeTime)
      },
      notificationGateway = notificationGateway,
      uniqueId = ReminderV2::uniqueId,
    )
}
