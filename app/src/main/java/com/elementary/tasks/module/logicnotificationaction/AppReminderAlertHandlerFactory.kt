package com.elementary.tasks.module.logicnotificationaction

import com.elementary.tasks.core.services.action.reminder.ReminderDataProvider
import com.elementary.tasks.core.services.action.reminder.process.ReminderNotificationHandler
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.reminder.v2.NotificationSettings
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.notificationaction.ActionHandler
import com.github.naz013.logic.notificationaction.LoudNotificationStyle
import com.github.naz013.logic.notificationaction.NotificationGateway
import com.github.naz013.logic.notificationaction.SilentNotificationStyle
import com.github.naz013.logic.notificationaction.WearNotification
import com.github.naz013.logic.notificationaction.WearPreferences
import com.github.naz013.logic.notificationaction.reminder.ReminderAlertHandlerFactory
import com.github.naz013.logic.reminder.ReminderPreferences
import com.github.naz013.notification.NotificationApi

/**
 * `logic-notification-action` needs the concrete "show the reminder alert" handler to target
 * `app`-only classes (`ReminderActionReceiver`, `ReminderActionActivity`, via
 * `ReminderNotificationHandler`), so this factory - the only place that construction happens -
 * lives in `app` and implements the module's [ReminderAlertHandlerFactory] seam.
 */
class AppReminderAlertHandlerFactory(
  private val reminderDataProvider: ReminderDataProvider,
  private val contextProvider: ContextProvider,
  private val textProvider: TextProvider,
  private val notificationGateway: NotificationGateway,
  private val wearPreferences: WearPreferences,
  private val notificationApi: NotificationApi,
  private val reminderPreferences: ReminderPreferences,
  private val wearNotification: WearNotification,
) : ReminderAlertHandlerFactory {
  override fun create(
    canShowWindow: Boolean,
    notificationSettings: NotificationSettings,
  ): ActionHandler<ReminderV2> =
    ReminderNotificationHandler(
      reminderDataProvider = reminderDataProvider,
      notificationSettings = notificationSettings,
      contextProvider = contextProvider,
      textProvider = textProvider,
      notificationGateway = notificationGateway,
      wearPreferences = wearPreferences,
      notificationApi = notificationApi,
      reminderPreferences = reminderPreferences,
      wearNotification = wearNotification,
      style = if (canShowWindow) LoudNotificationStyle else SilentNotificationStyle,
    )
}
