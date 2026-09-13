package com.elementary.tasks.pomodoro

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.feature.pomodoro.PomodoroPreferences

class PomodoroPreferencesImpl(
  private val prefs: Prefs,
) : PomodoroPreferences {
  override var workDurationMinutes: Int
    get() = prefs.pomodoroWorkDurationMinutes
    set(value) { prefs.pomodoroWorkDurationMinutes = value }

  override var breakDurationMinutes: Int
    get() = prefs.pomodoroBreakDurationMinutes
    set(value) { prefs.pomodoroBreakDurationMinutes = value }

  override var longBreakDurationMinutes: Int
    get() = prefs.pomodoroLongBreakDurationMinutes
    set(value) { prefs.pomodoroLongBreakDurationMinutes = value }

  override var sessionsUntilLongBreak: Int
    get() = prefs.pomodoroSessionsUntilLongBreak
    set(value) { prefs.pomodoroSessionsUntilLongBreak = value }

  override var autoStartNext: Boolean
    get() = prefs.pomodoroAutoStartNext
    set(value) { prefs.pomodoroAutoStartNext = value }
}
