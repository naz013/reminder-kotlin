package com.github.naz013.logic.habit.usecase

import com.github.naz013.datecalc.NowDateTimeProvider
import com.github.naz013.domain.habit.Habit
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logic.habit.HabitDetails
import com.github.naz013.logic.habit.HabitHistoryBuilder
import com.github.naz013.logic.habit.HabitStreakCalculator
import com.github.naz013.repository.HabitCheckInRepository
import com.github.naz013.repository.HabitRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.threeten.bp.LocalDate

/** Emits null when the habit does not exist (anymore). */
class ObserveHabitDetailsUseCase(
  private val habitRepository: HabitRepository,
  private val habitCheckInRepository: HabitCheckInRepository,
  private val streakCalculator: HabitStreakCalculator,
  private val historyBuilder: HabitHistoryBuilder,
  private val nowDateTimeProvider: NowDateTimeProvider,
  private val dispatcherProvider: DispatcherProvider
) {

  @OptIn(ExperimentalCoroutinesApi::class)
  operator fun invoke(habitId: String, weeks: Int): Flow<HabitDetails?> {
    return nowDateTimeProvider.observeToday()
      .flatMapLatest { today ->
        habitRepository.observeById(habitId).flatMapLatest { habit ->
          if (habit == null) flowOf(null) else observeDetails(habit, today, weeks)
        }
      }
      .flowOn(dispatcherProvider.io())
  }

  private fun observeDetails(habit: Habit, today: LocalDate, weeks: Int): Flow<HabitDetails?> {
    val from = today.minusDays(ObserveHabitsWithProgressUseCase.LOOKBACK_DAYS)
    return habitCheckInRepository.observeRange(habit.id, from, today).map { items ->
      HabitDetails(
        habit = habit,
        todayCount = items.filter { it.date == today }.sumOf { it.count },
        periodCount = streakCalculator.periodCount(habit, items, today),
        streak = streakCalculator.streak(habit, items, today),
        longestStreak = streakCalculator.longestStreak(habit, items),
        totalCheckIns = items.sumOf { it.count },
        days = historyBuilder.build(habit, items, today, weeks)
      )
    }
  }
}
