package com.github.naz013.logic.notificationaction.calendarevent

import com.github.naz013.domain.GoogleCalendarEvent
import com.github.naz013.logic.notificationaction.ActionHandler

/**
 * Seam for building the "show the alert notification" handler. Implemented in `app` - see
 * `AppGoogleCalendarEventAlertHandlerFactory` - because the concrete handler needs to target
 * `app`-only classes (`GoogleCalendarEventActionReceiver`, `BottomNavActivity`) that this module
 * can't reference. Mirrors `BirthdayAlertHandlerFactory`.
 */
fun interface GoogleCalendarEventAlertHandlerFactory {
  fun create(): ActionHandler<GoogleCalendarEvent>
}
