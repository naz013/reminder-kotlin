package com.elementary.tasks.reminder.build.valuedialog

import com.elementary.tasks.core.analytics.Traces
import com.elementary.tasks.reminder.build.valuedialog.controller.ValueController

class ValueDialogLifecycleDispatcher(
  private val controller: ValueController
) {

  var state = ValueDialogState.NONE
    private set

  fun dispatchOnCreate() {
    if (state != ValueDialogState.NONE) {
      return
    }
    Traces.d("ValueDialogLifecycleDispatcher", "dispatchOnCreate to $controller")
    state = ValueDialogState.CREATED
  }

  fun dispatchOnResume() {
    if (state != ValueDialogState.CREATED) {
      return
    }
    Traces.d("ValueDialogLifecycleDispatcher", "dispatchOnResume to $controller")
    state = ValueDialogState.RESUMED
  }

  fun dispatchOnStop() {
    if (state != ValueDialogState.RESUMED) {
      return
    }
    Traces.d("ValueDialogLifecycleDispatcher", "dispatchOnStop to $controller")
    controller.onStop()
    state = ValueDialogState.STOPPED
  }

  fun dispatchOnDestroy() {
    if (state != ValueDialogState.STOPPED) {
      return
    }
    Traces.d("ValueDialogLifecycleDispatcher", "dispatchOnDestroy to $controller")
    controller.onDestroy()
    state = ValueDialogState.DESTROYED
  }
}
