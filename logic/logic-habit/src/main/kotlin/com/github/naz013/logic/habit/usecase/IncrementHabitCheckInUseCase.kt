package com.github.naz013.logic.habit.usecase

import com.github.naz013.datecalc.NowDateTimeProvider
import com.github.naz013.domain.habit.HabitCheckIn
import com.github.naz013.logging.Logger
import com.github.naz013.repository.HabitCheckInRepository
import com.github.naz013.repository.HabitRepository
import org.threeten.bp.LocalDate

class IncrementHabitCheckInUseCase(
  private val habitRepository: HabitRepository,
  private val habitCheckInRepository: HabitCheckInRepository,
  private val nowDateTimeProvider: NowDateTimeProvider
) {
  suspend operator fun invoke(habitId: String, date: LocalDate = nowDateTimeProvider.nowDate()) {
    val habit = habitRepository.getById(habitId)
    if (habit == null) {
      Logger.w(TAG, "Habit not found: $habitId")
      return
    }
    val now = nowDateTimeProvider.nowDateTime()
    val existing = habitCheckInRepository.get(habitId, date)
    val count = (existing?.count ?: 0) + 1
    habitCheckInRepository.save(
      HabitCheckIn(
        habitId = habitId,
        date = date,
        count = count,
        isCompleted = count >= habit.targetCount,
        createdAt = existing?.createdAt ?: now,
        updatedAt = now
      )
    )
    Logger.i(TAG, "Incremented check-in for habit $habitId on $date to $count")
  }

  companion object {
    private const val TAG = "IncrementHabitCheckIn"
  }
}
