package com.github.naz013.logic.habit.nudge

import com.github.naz013.datecalc.NowDateTimeProvider
import com.github.naz013.domain.habit.Habit
import com.github.naz013.logging.Logger
import com.github.naz013.logic.habit.HabitStreakCalculator
import com.github.naz013.logic.habit.HabitWithProgress
import com.github.naz013.logic.habit.usecase.CheckInHabitUseCase
import com.github.naz013.logic.routine.HabitConfig
import com.github.naz013.repository.HabitCheckInRepository
import com.github.naz013.repository.HabitRepository

class HabitNudgeProcessor(
  private val habitRepository: HabitRepository,
  private val habitCheckInRepository: HabitCheckInRepository,
  private val streakCalculator: HabitStreakCalculator,
  private val calculator: HabitNudgeCalculator,
  private val nowDateTimeProvider: NowDateTimeProvider,
  private val habitConfig: HabitConfig,
  private val scheduleHabitNudgeUseCase: ScheduleHabitNudgeUseCase,
  private val checkInHabitUseCase: CheckInHabitUseCase,
  private val notifier: HabitNudgeNotifier
) {

  suspend fun onNudgeDue(habitId: String) {
    val habit = habitRepository.getById(habitId)
    if (habit == null) {
      Logger.w(TAG, "Habit not found: $habitId")
      return
    }
    scheduleHabitNudgeUseCase(habit)
    if (!habitConfig.isEnabled || habit.reminderTime == null) return
    if (calculator.shouldNotify(progress(habit))) {
      notifier.show(habit)
    } else {
      Logger.d(TAG, "Skipping nudge for ${habit.id}: already done")
    }
  }

  suspend fun onCheckIn(habitId: String) {
    notifier.cancel(habitId)
    val habit = habitRepository.getById(habitId) ?: return
    checkInHabitUseCase(progress(habit))
  }

  private suspend fun progress(habit: Habit): HabitWithProgress {
    val today = nowDateTimeProvider.nowDate()
    val checkIns = habitCheckInRepository.getRange(habit.id, today.minusDays(WEEK_LOOKBACK_DAYS), today)
    return HabitWithProgress(
      habit = habit,
      todayCount = checkIns.filter { it.date == today }.sumOf { it.count },
      periodCount = streakCalculator.periodCount(habit, checkIns, today),
      streak = 0
    )
  }

  companion object {
    private const val TAG = "HabitNudgeProcessor"
    private const val WEEK_LOOKBACK_DAYS = 6L
  }
}
