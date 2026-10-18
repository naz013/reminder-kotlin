package com.github.naz013.logic.habit.usecase

import com.github.naz013.datecalc.NowDateTimeProvider
import com.github.naz013.domain.habit.Habit
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logic.habit.HabitStreakCalculator
import com.github.naz013.logic.habit.HabitWithProgress
import com.github.naz013.repository.HabitCheckInRepository
import com.github.naz013.repository.HabitRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class ObserveHabitsWithProgressUseCase(
  private val habitRepository: HabitRepository,
  private val habitCheckInRepository: HabitCheckInRepository,
  private val streakCalculator: HabitStreakCalculator,
  private val nowDateTimeProvider: NowDateTimeProvider,
  private val dispatcherProvider: DispatcherProvider
) {

  @OptIn(ExperimentalCoroutinesApi::class)
  operator fun invoke(): Flow<List<HabitWithProgress>> {
    return habitRepository.observeAll()
      .flatMapLatest { habits -> observeProgress(habits) }
      .flowOn(dispatcherProvider.io())
  }

  private fun observeProgress(habits: List<Habit>): Flow<List<HabitWithProgress>> {
    if (habits.isEmpty()) return flowOf(emptyList())
    val today = nowDateTimeProvider.nowDate()
    val from = today.minusDays(LOOKBACK_DAYS)
    val flows = habits.map { habit ->
      habitCheckInRepository.observeRange(habit.id, from, today).map { items ->
        HabitWithProgress(
          habit = habit,
          todayCount = items.filter { it.date == today }.sumOf { it.count },
          periodCount = streakCalculator.periodCount(habit, items, today),
          streak = streakCalculator.streak(habit, items, today)
        )
      }
    }
    return combine(flows) { it.toList() }
  }

  companion object {
    /** Streaks longer than this are reported as capped at the window the check-ins are read for. */
    const val LOOKBACK_DAYS = 400L
  }
}
