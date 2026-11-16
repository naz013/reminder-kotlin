package com.github.naz013.appwidgets.pomodoro

import com.github.naz013.feature.pomodoro.engine.PomodoroMode

internal data class PomodoroAppWidgetState(
  val isUnlocked: Boolean = true,
  val mode: PomodoroMode = PomodoroMode.Idle,
  val isPaused: Boolean = false,
  val phaseLabel: String = "",
  val timeLabel: String = "",
  val focusTodayLabel: String = ""
) {
  val isIdle: Boolean
    get() = mode == PomodoroMode.Idle
}
