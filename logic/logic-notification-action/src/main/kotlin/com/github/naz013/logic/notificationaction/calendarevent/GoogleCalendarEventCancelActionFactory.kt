package com.github.naz013.logic.notificationaction.calendarevent

import com.github.naz013.domain.GoogleCalendarEvent
import com.github.naz013.logic.notificationaction.ActionHandler
import com.github.naz013.logic.notificationaction.CancelNotificationDecorator
import com.github.naz013.logic.notificationaction.NotificationGateway

/**
 * A calendar event notification only ever needs "acknowledge and clear it" - unlike a reminder
 * there's nothing to complete/snooze, and unlike a birthday there's no year-shown bookkeeping - so
 * the delegate is a no-op; [CancelNotificationDecorator] does the actual dismiss.
 */
class GoogleCalendarEventCancelActionFactory(
  private val notificationGateway: NotificationGateway,
) {
  fun createCancel(): ActionHandler<GoogleCalendarEvent> =
    CancelNotificationDecorator(
      delegate = ActionHandler { },
      notificationGateway = notificationGateway,
      uniqueId = GoogleCalendarEvent::uniqueId,
    )
}
