package com.elementary.tasks.reminder.build.logic.builderstate

import com.elementary.tasks.reminder.build.BuilderState
import com.elementary.tasks.reminder.build.EmptyState
import com.elementary.tasks.reminder.build.ReadyState
import com.elementary.tasks.reminder.build.reminder.compose.ComposedRecurrence
import com.github.naz013.logging.Logger

class BuilderStateCalculator {
  operator fun invoke(composedRecurrence: ComposedRecurrence?): BuilderState =
    if (composedRecurrence != null) {
      Logger.i(TAG, "Builder state is ready")
      ReadyState
    } else {
      Logger.i(TAG, "Builder state is empty")
      EmptyState
    }

  companion object {
    private const val TAG = "BuilderStateCalculator"
  }
}
