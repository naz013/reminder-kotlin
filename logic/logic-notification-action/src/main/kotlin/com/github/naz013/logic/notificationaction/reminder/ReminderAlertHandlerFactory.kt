package com.github.naz013.logic.notificationaction.reminder

import com.github.naz013.domain.reminder.v2.NotificationSettings
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.notificationaction.ActionHandler

/**
 * Seam for building the "show the alert notification" handler. Implemented in `app` - see
 * `AppReminderAlertHandlerFactory` - because the concrete handler needs to target `app`-only
 * classes (`ReminderActionReceiver`, `ReminderActionActivity`) that this module can't reference.
 */
fun interface ReminderAlertHandlerFactory {
  fun create(canShowWindow: Boolean, notificationSettings: NotificationSettings): ActionHandler<ReminderV2>
}
