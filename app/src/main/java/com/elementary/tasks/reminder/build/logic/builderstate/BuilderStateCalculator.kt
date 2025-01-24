package com.elementary.tasks.reminder.build.logic.builderstate

import com.elementary.tasks.reminder.build.BuilderState
import com.elementary.tasks.reminder.build.EmptyState
import com.elementary.tasks.reminder.build.ReadyState
import com.github.naz013.logging.Logger

class BuilderStateCalculator {

  operator fun invoke(type: Int): BuilderState {
    return if (type != 0) {
      Logger.i(TAG, "Builder state is ready")
      ReadyState
    } else {
      Logger.i(TAG, "Builder state is empty")
      EmptyState
    }
  }

  companion object {
    private const val TAG = "BuilderStateCalculator"
  }
}
