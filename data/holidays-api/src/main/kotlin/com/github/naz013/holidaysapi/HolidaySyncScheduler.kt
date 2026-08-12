package com.github.naz013.holidaysapi

/**
 * Facade over the background sync of public holiday data, so callers (e.g. Calendar Settings)
 * never need a direct dependency on `work-api`.
 */
interface HolidaySyncScheduler {
  /** Enqueues an immediate sync plus a periodic update-check, e.g. when the feature is toggled on. */
  fun enable()

  /** Cancels the periodic update-check. Cached data is left in place. */
  fun disable()

  /** Re-triggers an immediate sync, e.g. after the selected country changes. */
  fun syncNow()
}
