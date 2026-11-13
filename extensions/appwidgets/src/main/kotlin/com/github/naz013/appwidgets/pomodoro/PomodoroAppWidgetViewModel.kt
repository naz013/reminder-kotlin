package com.github.naz013.appwidgets.pomodoro

import com.github.naz013.common.TextProvider
import com.github.naz013.feature.pomodoro.engine.PomodoroEngineState
import com.github.naz013.feature.pomodoro.engine.PomodoroMode
import com.github.naz013.feature.pomodoro.engine.PomodoroTimerEngine
import com.github.naz013.platform.BuildInfo
import com.github.naz013.repository.PomodoroSessionRepository
import com.github.naz013.ui.common.R
import org.threeten.bp.LocalDate
import java.util.Locale

internal class PomodoroAppWidgetViewModel(
  private val buildInfo: BuildInfo,
  private val pomodoroTimerEngine: PomodoroTimerEngine,
  private val pomodoroSessionRepository: PomodoroSessionRepository,
  private val textProvider: TextProvider,
  private val timeFormatter: PomodoroWidgetTimeFormatter,
  private val nowMillis: () -> Long = System::currentTimeMillis
) {

  suspend fun getState(): PomodoroAppWidgetState {
    if (!buildInfo.isPro) {
      return PomodoroAppWidgetState(isUnlocked = false)
    }
    val engineState = pomodoroTimerEngine.state.value
    val today = LocalDate.now()
    val focusMinutes = pomodoroSessionRepository.getByDateRange(today, today)
      .sumOf { it.actualFocusSeconds } / SECONDS_PER_MINUTE
    val focusLabel = textProvider.getString(R.string.widget_pomodoro_today_minutes, focusMinutes)
    if (!engineState.isRunning) {
      return PomodoroAppWidgetState(focusTodayLabel = focusLabel)
    }
    return PomodoroAppWidgetState(
      mode = engineState.mode,
      isPaused = engineState.isPaused,
      phaseLabel = textProvider.getString(engineState.mode.labelRes()),
      timeLabel = timeLabel(engineState),
      focusTodayLabel = focusLabel
    )
  }

  private fun timeLabel(state: PomodoroEngineState): String {
    return if (state.isPaused) {
      textProvider.getString(R.string.widget_pomodoro_paused_remaining, formatRemaining(state.remainingSeconds))
    } else {
      val endTime = nowMillis() + state.remainingSeconds * MILLIS_PER_SECOND
      textProvider.getString(R.string.widget_pomodoro_ends_at, timeFormatter.formatClock(endTime))
    }
  }

  private fun formatRemaining(seconds: Int): String {
    return String.format(Locale.getDefault(), "%d:%02d", seconds / SECONDS_PER_MINUTE, seconds % SECONDS_PER_MINUTE)
  }

  private fun PomodoroMode.labelRes(): Int {
    return when (this) {
      PomodoroMode.Break -> R.string.pomodoro_break
      PomodoroMode.LongBreak -> R.string.pomodoro_long_break
      PomodoroMode.Work -> R.string.pomodoro_work
      PomodoroMode.Idle -> R.string.pomodoro_ready
    }
  }

  companion object {
    private const val SECONDS_PER_MINUTE = 60
    private const val MILLIS_PER_SECOND = 1000L
  }
}
