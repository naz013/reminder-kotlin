package com.github.naz013.logic.habit.usecase

import com.github.naz013.datecalc.NowDateTimeProvider
import com.github.naz013.domain.habit.HabitCheckIn
import com.github.naz013.logging.Logger
import com.github.naz013.repository.HabitCheckInRepository
import com.github.naz013.repository.HabitRepository
import org.threeten.bp.LocalDate

/** Marks the day fully done (count = target), or clears any progress if the day already has some. */
class ToggleHabitCheckInUseCase(
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
    val existing = habitCheckInRepository.get(habitId, date)
    if (existing != null && existing.count > 0) {
      habitCheckInRepository.delete(habitId, date)
      Logger.i(TAG, "Cleared check-in for habit $habitId on $date")
      return
    }
    val now = nowDateTimeProvider.nowDateTime()
    habitCheckInRepository.save(
      HabitCheckIn(
        habitId = habitId,
        date = date,
        count = habit.targetCount.coerceAtLeast(1),
        isCompleted = true,
        createdAt = existing?.createdAt ?: now,
        updatedAt = now
      )
    )
    Logger.i(TAG, "Completed check-in for habit $habitId on $date")
  }

  companion object {
    private const val TAG = "ToggleHabitCheckIn"
  }
}
