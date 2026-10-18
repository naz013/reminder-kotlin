package com.github.naz013.logic.habit.usecase

import com.github.naz013.domain.habit.Habit
import com.github.naz013.repository.HabitRepository

class GetHabitUseCase(
  private val habitRepository: HabitRepository
) {
  suspend operator fun invoke(id: String): Habit? = habitRepository.getById(id)
}
