package com.github.naz013.logic.habit.nudge

import com.github.naz013.domain.habit.Habit

/** Implemented in `app`, which owns the receivers and activities the notification must target. */
interface HabitNudgeNotifier {
  fun show(habit: Habit)

  fun cancel(habitId: String)
}
