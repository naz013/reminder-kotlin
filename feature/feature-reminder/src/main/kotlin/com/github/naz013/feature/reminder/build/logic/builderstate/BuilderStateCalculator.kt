package com.github.naz013.feature.reminder.build.logic.builderstate

import com.github.naz013.feature.reminder.build.BuilderState
import com.github.naz013.feature.reminder.build.EmptyState
import com.github.naz013.feature.reminder.build.ReadyState
import com.github.naz013.feature.reminder.build.reminder.compose.ComposedRecurrence
import com.github.naz013.logging.Logger

internal class BuilderStateCalculator {
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
