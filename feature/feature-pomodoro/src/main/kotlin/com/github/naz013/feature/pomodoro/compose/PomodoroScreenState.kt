package com.github.naz013.feature.pomodoro.compose

import com.github.naz013.feature.pomodoro.engine.PomodoroMode

internal data class PomodoroScreenState(
  val mode: PomodoroMode = PomodoroMode.Idle,
  val isRunning: Boolean = false,
  val isPaused: Boolean = false,
  val remainingSeconds: Int = 0,
  val totalSeconds: Int = 0,
  val timeLabel: String = "00:00",
  val progress: Float = 0f,
  val completedWorkSessionsToday: Int = 0,
  val linkedReminderId: String? = null,
  val linkedReminderSummary: String? = null,
  val isReminderPickerVisible: Boolean = false,
  val availableReminders: List<UiPickableReminder> = emptyList(),
)

internal data class UiPickableReminder(
  val id: String,
  val summary: String,
)
