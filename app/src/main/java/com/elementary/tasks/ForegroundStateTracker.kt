package com.elementary.tasks

import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tracks whether any Activity of the app is currently resumed, driven by [ActivityObserver]'s
 * app-wide [android.app.Application.ActivityLifecycleCallbacks]. A resumed-count above zero means
 * some UI is visible - used to decide whether an in-app notification banner can be shown (see
 * `com.elementary.tasks.core.services.action.inapp.InAppAlertBus`) instead of relying solely on
 * the system notification.
 */
class ForegroundStateTracker {
  private val resumedCount = AtomicInteger(0)
  private val _isForeground = MutableStateFlow(false)
  val isForeground: StateFlow<Boolean> = _isForeground.asStateFlow()

  fun onResumed() {
    if (resumedCount.incrementAndGet() == 1) {
      _isForeground.value = true
    }
  }

  fun onPaused() {
    if (resumedCount.decrementAndGet() <= 0) {
      _isForeground.value = false
    }
  }
}
