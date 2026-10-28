package com.github.naz013.logic.habit.nudge

import com.github.naz013.logging.Logger
import com.github.naz013.workapi.BackgroundTask
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.TaskProgressReporter
import com.github.naz013.workapi.TaskResult

class HabitNudgeTask(
  private val habitNudgeProcessor: HabitNudgeProcessor
) : BackgroundTask {
  override suspend fun run(input: TaskData, progress: TaskProgressReporter): TaskResult {
    val habitId = input.getString(KEY_HABIT_ID)
    if (habitId == null) {
      Logger.w(TASK_KEY, "Missing habit id")
      return TaskResult.Success
    }
    habitNudgeProcessor.onNudgeDue(habitId)
    return TaskResult.Success
  }

  companion object {
    const val TASK_KEY = "habit_nudge"
    const val KEY_HABIT_ID = "habit_id"

    fun tagFor(habitId: String) = "${TASK_KEY}_$habitId"
  }
}
