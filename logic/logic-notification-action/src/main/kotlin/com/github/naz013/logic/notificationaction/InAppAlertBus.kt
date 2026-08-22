package com.github.naz013.logic.notificationaction

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds at most one in-app alert at a time - a newer [show] call always replaces whatever is
 * currently there rather than queuing behind it, so reminders/birthdays that fire in a burst
 * (e.g. several birthdays due the same day, processed in one loop) never stack banners; only the
 * most recently emitted alert is ever visible.
 */
class InAppAlertBus {
  private val _current = MutableStateFlow<InAppAlert?>(null)
  val current: StateFlow<InAppAlert?> = _current.asStateFlow()

  fun show(alert: InAppAlert) {
    _current.value = alert
  }

  /** No-op if [alertId] no longer matches the current alert - it was already superseded. */
  fun clear(alertId: String) {
    if (_current.value?.alertId == alertId) {
      _current.value = null
    }
  }
}
