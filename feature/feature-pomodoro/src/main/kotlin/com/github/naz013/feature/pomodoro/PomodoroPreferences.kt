package com.github.naz013.feature.pomodoro

/** Seam implemented in `app` (mirrors `ReminderPreferences`), so this Compose/engine module never
 * has to depend on `app`'s `Prefs`. */
interface PomodoroPreferences {
  var workDurationMinutes: Int
  var breakDurationMinutes: Int
  var longBreakDurationMinutes: Int
  var sessionsUntilLongBreak: Int
  var autoStartNext: Boolean
}
