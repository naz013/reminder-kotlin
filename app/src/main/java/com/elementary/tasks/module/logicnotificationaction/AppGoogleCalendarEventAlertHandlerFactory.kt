package com.elementary.tasks.module.logicnotificationaction

import com.elementary.tasks.core.services.action.calendarevent.process.GoogleCalendarEventNotificationHandler
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.GoogleCalendarEvent
import com.github.naz013.logic.notificationaction.ActionHandler
import com.github.naz013.logic.notificationaction.LoudNotificationStyle
import com.github.naz013.logic.notificationaction.NotificationGateway
import com.github.naz013.logic.notificationaction.WearNotification
import com.github.naz013.logic.notificationaction.WearPreferences
import com.github.naz013.logic.notificationaction.calendarevent.GoogleCalendarEventAlertHandlerFactory

/**
 * `logic-notification-action` needs the concrete "show the calendar event alert" handler to
 * target `app`-only classes (`GoogleCalendarEventActionReceiver`, `BottomNavActivity`, via
 * `GoogleCalendarEventNotificationHandler`), so this factory - the only place that construction
 * happens - lives in `app` and implements the module's [GoogleCalendarEventAlertHandlerFactory]
 * seam. Mirrors `AppBirthdayAlertHandlerFactory`.
 */
class AppGoogleCalendarEventAlertHandlerFactory(
  private val contextProvider: ContextProvider,
  private val textProvider: TextProvider,
  private val notificationGateway: NotificationGateway,
  private val wearPreferences: WearPreferences,
  private val wearNotification: WearNotification,
) : GoogleCalendarEventAlertHandlerFactory {
  override fun create(): ActionHandler<GoogleCalendarEvent> =
    GoogleCalendarEventNotificationHandler(
      contextProvider = contextProvider,
      textProvider = textProvider,
      notificationGateway = notificationGateway,
      wearPreferences = wearPreferences,
      wearNotification = wearNotification,
      style = LoudNotificationStyle,
    )
}
