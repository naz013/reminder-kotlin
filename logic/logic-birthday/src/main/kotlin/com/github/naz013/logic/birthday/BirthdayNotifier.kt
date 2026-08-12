package com.github.naz013.logic.birthday

/**
 * Seam over app's `Notifier.showBirthdayPermanent`, which `logic-birthday`/`feature-birthday`
 * can't depend on. Implemented in `app` by delegating to `Notifier` - see `AppBirthdayNotifier`.
 */
interface BirthdayNotifier {
  fun showBirthdayPermanent()
}
