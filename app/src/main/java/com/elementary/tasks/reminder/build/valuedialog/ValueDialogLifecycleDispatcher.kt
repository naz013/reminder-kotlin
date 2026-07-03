package com.elementary.tasks.reminder.build.valuedialog

import com.elementary.tasks.reminder.build.valuedialog.controller.ValueController
import com.github.naz013.logging.Logger

class ValueDialogLifecycleDispatcher(
  private val controller: ValueController,
) {
  var state = ValueDialogState.NONE
    private set

  fun dispatchOnCreate() {
    if (state != ValueDialogState.NONE) {
      return
    }
    Logger.d(TAG, "dispatchOnCreate to $controller")
    state = ValueDialogState.CREATED
  }

  fun dispatchOnResume() {
    if (state != ValueDialogState.CREATED) {
      return
    }
    Logger.d(TAG, "dispatchOnResume to $controller")
    state = ValueDialogState.RESUMED
  }

  fun dispatchOnStop() {
    if (state != ValueDialogState.RESUMED) {
      return
    }
    Logger.d(TAG, "dispatchOnStop to $controller")
    controller.onStop()
    state = ValueDialogState.STOPPED
  }

  fun dispatchOnDestroy() {
    if (state != ValueDialogState.STOPPED) {
      return
    }
    Logger.d(TAG, "dispatchOnDestroy to $controller")
    controller.onDestroy()
    state = ValueDialogState.DESTROYED
  }

  companion object {
    private const val TAG = "ValueDialogLifecycleDispatcher"
  }
}
