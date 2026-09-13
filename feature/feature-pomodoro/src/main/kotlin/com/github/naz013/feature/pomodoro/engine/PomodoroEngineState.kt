package com.github.naz013.feature.pomodoro.engine

data class PomodoroEngineState(
  val mode: PomodoroMode = PomodoroMode.Idle,
  val isRunning: Boolean = false,
  val isPaused: Boolean = false,
  val remainingSeconds: Int = 0,
  val totalSeconds: Int = 0,
  val linkedReminderId: String? = null,
  val completedWorkSessionsToday: Int = 0,
)
