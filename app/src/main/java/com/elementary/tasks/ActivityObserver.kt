package com.elementary.tasks

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.elementary.tasks.core.os.ContextSwitcher

class ActivityObserver(
  private val contextSwitcher: ContextSwitcher
) : ActivityLifecycleCallbacks {

  override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    contextSwitcher.switchContext(activity)
  }

  override fun onActivityStarted(activity: Activity) {
  }

  override fun onActivityResumed(activity: Activity) {
  }

  override fun onActivityPaused(activity: Activity) {
  }

  override fun onActivityStopped(activity: Activity) {
  }

  override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
  }

  override fun onActivityDestroyed(activity: Activity) {
  }
}
