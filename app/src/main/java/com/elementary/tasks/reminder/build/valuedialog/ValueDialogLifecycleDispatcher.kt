package com.elementary.tasks.reminder.build.valuedialog

import com.elementary.tasks.reminder.build.valuedialog.controller.ValueController
import timber.log.Timber

class ValueDialogLifecycleDispatcher(
  private val controller: ValueController
) {

  var state = ValueDialogState.NONE
    private set

  fun dispatchOnCreate() {
    if (state != ValueDialogState.NONE) {
      return
    }
    Timber.d("dispatchOnCreate to $controller")
    state = ValueDialogState.CREATED
  }

  fun dispatchOnResume() {
    if (state != ValueDialogState.CREATED) {
      return
    }
    Timber.d("dispatchOnResume to $controller")
    state = ValueDialogState.RESUMED
  }

  fun dispatchOnStop() {
    if (state != ValueDialogState.RESUMED) {
      return
    }
    Timber.d("dispatchOnStop to $controller")
    controller.onStop()
    state = ValueDialogState.STOPPED
  }

  fun dispatchOnDestroy() {
    if (state != ValueDialogState.STOPPED) {
      return
    }
    Timber.d("dispatchOnDestroy to $controller")
    controller.onDestroy()
    state = ValueDialogState.DESTROYED
  }
}
