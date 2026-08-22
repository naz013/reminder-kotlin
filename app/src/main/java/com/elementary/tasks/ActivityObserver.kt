package com.elementary.tasks

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.elementary.tasks.core.os.ContextSwitcher

class ActivityObserver(
  private val contextSwitcher: ContextSwitcher,
  private val foregroundStateTracker: ForegroundStateTracker,
) : ActivityLifecycleCallbacks {
  override fun onActivityCreated(
    activity: Activity,
    savedInstanceState: Bundle?,
  ) {
    contextSwitcher.switchContext(activity)
  }

  override fun onActivityStarted(activity: Activity) {
  }

  override fun onActivityResumed(activity: Activity) {
    foregroundStateTracker.onResumed()
  }

  override fun onActivityPaused(activity: Activity) {
    foregroundStateTracker.onPaused()
  }

  override fun onActivityStopped(activity: Activity) {
  }

  override fun onActivitySaveInstanceState(
    activity: Activity,
    outState: Bundle,
  ) {
  }

  override fun onActivityDestroyed(activity: Activity) {
  }
}
