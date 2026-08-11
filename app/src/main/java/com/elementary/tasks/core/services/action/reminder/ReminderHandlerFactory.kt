package com.elementary.tasks.core.services.action.reminder

import com.elementary.tasks.core.services.action.ActionHandler
import com.elementary.tasks.core.services.action.CancelNotificationDecorator
import com.elementary.tasks.core.services.action.LoudNotificationStyle
import com.elementary.tasks.core.services.action.SilentNotificationStyle
import com.elementary.tasks.core.services.action.WearNotification
import com.elementary.tasks.core.services.action.reminder.process.ReminderNotificationHandler
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.logic.reminder.usecase.CompleteReminderUseCase
import com.elementary.tasks.reminder.scheduling.usecase.SnoozeReminderUseCase
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.reminder.v2.NotificationSettings
import com.github.naz013.domain.reminder.v2.ReminderV2

class ReminderHandlerFactory(
  private val reminderDataProvider: ReminderDataProvider,
  private val contextProvider: ContextProvider,
  private val textProvider: TextProvider,
  private val notifier: Notifier,
  private val prefs: Prefs,
  private val wearNotification: WearNotification,
  private val completeReminderUseCase: CompleteReminderUseCase,
  private val snoozeReminderUseCase: SnoozeReminderUseCase,
) {
  fun createAction(
    canShowWindow: Boolean,
    notificationSettings: NotificationSettings,
  ): ActionHandler<ReminderV2> =
    ReminderNotificationHandler(
      reminderDataProvider = reminderDataProvider,
      notificationSettings = notificationSettings,
      contextProvider = contextProvider,
      textProvider = textProvider,
      notifier = notifier,
      prefs = prefs,
      wearNotification = wearNotification,
      style = if (canShowWindow) LoudNotificationStyle else SilentNotificationStyle,
    )

  fun createComplete(): ActionHandler<ReminderV2> =
    CancelNotificationDecorator(
      delegate = ActionHandler { reminder: ReminderV2 -> completeReminderUseCase(reminder) },
      notifier = notifier,
      uniqueId = ReminderV2::uniqueId,
    )

  fun createSnooze(): ActionHandler<ReminderV2> =
    CancelNotificationDecorator(
      delegate =
        ActionHandler { reminder: ReminderV2 ->
          snoozeReminderUseCase(reminder = reminder, timeInMinutes = prefs.snoozeTime)
        },
      notifier = notifier,
      uniqueId = ReminderV2::uniqueId,
    )
}
