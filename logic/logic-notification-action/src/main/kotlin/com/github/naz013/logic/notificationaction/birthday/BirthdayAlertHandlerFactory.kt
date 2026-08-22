package com.github.naz013.logic.notificationaction.birthday

import com.github.naz013.domain.Birthday
import com.github.naz013.logic.notificationaction.ActionHandler

/**
 * Seam for building the "show the alert notification" handler. Implemented in `app` - see
 * `AppBirthdayAlertHandlerFactory` - because the concrete handler needs to target `app`-only
 * classes (`BirthdayActionReceiver`, `BirthdayActionActivity`) that this module can't reference.
 */
fun interface BirthdayAlertHandlerFactory {
  fun create(canPlaySound: Boolean): ActionHandler<Birthday>
}
