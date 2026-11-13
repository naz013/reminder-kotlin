package com.github.naz013.appwidgets.routine

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.routine.Routine
import com.github.naz013.feature.routine.run.RoutineRunEngine
import com.github.naz013.feature.routine.run.RoutineRunState
import com.github.naz013.logic.routine.RoutineConfig
import com.github.naz013.logic.routine.RoutineDurationCalculator
import com.github.naz013.repository.RoutineRepository
import com.github.naz013.ui.common.R
import com.github.naz013.ui.routine.RoutineColors
import com.github.naz013.ui.routine.RoutineIconSet

internal class RoutineAppWidgetViewModel(
  private val prefsProvider: RoutineWidgetPrefsProvider,
  private val routineConfig: RoutineConfig,
  private val routineRepository: RoutineRepository,
  private val routineRunEngine: RoutineRunEngine,
  private val routineDurationCalculator: RoutineDurationCalculator,
  private val textProvider: TextProvider
) {

  suspend fun getState(): RoutineAppWidgetState {
    val widgetId = prefsProvider.widgetId
    if (!routineConfig.isEnabled) {
      return RoutineAppWidgetState(widgetId = widgetId, isUnlocked = false)
    }
    val configuredId = prefsProvider.getRoutineId()
    val routine = if (configuredId != null) {
      routineRepository.getById(configuredId)
    } else {
      pickNextScheduled(routineRepository.getAll())
    }
    return RoutineAppWidgetState(widgetId = widgetId, item = routine?.toWidgetItem())
  }

  private fun pickNextScheduled(routines: List<Routine>): Routine? {
    return routines
      .filter { it.recurrence != null && it.steps.isNotEmpty() }
      .sortedWith(
        compareBy<Routine> { it.isDoneToday() }
          .thenBy { routine -> routine.steps.mapNotNull { it.scheduledTime }.minOrNull() ?: NO_TIME }
          .thenBy { it.title }
      )
      .firstOrNull()
  }

  private fun Routine.isDoneToday(): Boolean {
    return steps.isNotEmpty() && steps.all { it.isCompleted }
  }

  private fun Routine.toWidgetItem(): UiRoutineWidgetItem {
    val background = RoutineColors.ALL.getOrElse(color) { DEFAULT_COLOR }
    val foreground = if (background.luminance() > CONTRAST_LUMINANCE_THRESHOLD) Color.Black else Color.White
    val isRunning = isRunning(id)
    return UiRoutineWidgetItem(
      id = id,
      name = title,
      iconRes = icon?.let { RoutineIconSet.ALL.getOrNull(it) },
      backgroundColor = background.toArgb(),
      foregroundColor = foreground.toArgb(),
      stepCountLabel = textProvider.getString(R.string.routine_step_count, steps.size),
      durationLabel = routineDurationCalculator.formatDuration(totalDurationSeconds),
      statusLabel = statusLabel(isRunning),
      isRunning = isRunning
    )
  }

  private fun Routine.statusLabel(isRunning: Boolean): String {
    val completed = steps.count { it.isCompleted }
    return when {
      isRunning -> textProvider.getString(R.string.widget_routine_status_in_progress)
      isDoneToday() -> textProvider.getString(R.string.widget_routine_status_done)
      completed > 0 -> textProvider.getString(R.string.widget_routine_status_progress, completed, steps.size)
      else -> textProvider.getString(R.string.widget_routine_status_not_started)
    }
  }

  private fun isRunning(routineId: String): Boolean {
    return when (val runState = routineRunEngine.state.value) {
      is RoutineRunState.Running -> runState.routineId == routineId
      is RoutineRunState.Loading -> runState.routineId == routineId
      else -> false
    }
  }

  companion object {
    private const val NO_TIME = "99:99"
    private const val CONTRAST_LUMINANCE_THRESHOLD = 0.5f
    private val DEFAULT_COLOR = Color(0xFF86E3CE)
  }
}
