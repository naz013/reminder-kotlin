package com.github.naz013.logic.reminder

/**
 * Seam over app's `Notifier`, which `logic-reminder`/`feature-reminder` can't depend on.
 * Implemented in `app` by delegating to `Notifier` - see `AppReminderNotifier`.
 */
interface ReminderNotifier {
  fun showFavoriteNotification(text: String, notificationId: Int)
}
