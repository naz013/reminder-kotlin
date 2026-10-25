package com.github.naz013.logic.habit

import com.github.naz013.logic.habit.usecase.CheckInHabitUseCase
import com.github.naz013.logic.habit.usecase.DeleteHabitUseCase
import com.github.naz013.logic.habit.usecase.GetHabitUseCase
import com.github.naz013.logic.habit.usecase.IncrementHabitCheckInUseCase
import com.github.naz013.logic.habit.usecase.ObserveHabitDetailsUseCase
import com.github.naz013.logic.habit.usecase.ObserveHabitsWithProgressUseCase
import com.github.naz013.logic.habit.usecase.SaveHabitUseCase
import com.github.naz013.logic.habit.usecase.ToggleHabitCheckInUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val logicHabitModule = module {
  factoryOf(::HabitStreakCalculator)
  factoryOf(::HabitHistoryBuilder)

  factoryOf(::ObserveHabitsWithProgressUseCase)
  factoryOf(::ObserveHabitDetailsUseCase)
  factoryOf(::CheckInHabitUseCase)
  factoryOf(::ToggleHabitCheckInUseCase)
  factoryOf(::IncrementHabitCheckInUseCase)
  factoryOf(::SaveHabitUseCase)
  factoryOf(::DeleteHabitUseCase)
  factoryOf(::GetHabitUseCase)
}
