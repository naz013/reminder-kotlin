package com.github.naz013.logic.habit

import com.github.naz013.logic.habit.nudge.HabitNudgeCalculator
import com.github.naz013.logic.habit.nudge.HabitNudgeProcessor
import com.github.naz013.logic.habit.nudge.HabitNudgeTask
import com.github.naz013.logic.habit.nudge.RescheduleHabitNudgesUseCase
import com.github.naz013.logic.habit.nudge.ScheduleHabitNudgeUseCase
import com.github.naz013.logic.habit.usecase.CheckInHabitUseCase
import com.github.naz013.logic.habit.usecase.DeleteHabitUseCase
import com.github.naz013.logic.habit.usecase.GetHabitUseCase
import com.github.naz013.logic.habit.usecase.IncrementHabitCheckInUseCase
import com.github.naz013.logic.habit.usecase.ObserveHabitDetailsUseCase
import com.github.naz013.logic.habit.usecase.ObserveHabitsWithProgressUseCase
import com.github.naz013.logic.habit.usecase.SaveHabitUseCase
import com.github.naz013.logic.habit.usecase.ToggleHabitCheckInUseCase
import com.github.naz013.workapi.BackgroundTask
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
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

  factoryOf(::HabitNudgeCalculator)
  factoryOf(::ScheduleHabitNudgeUseCase)
  factoryOf(::RescheduleHabitNudgesUseCase)
  factoryOf(::HabitNudgeProcessor)
  factory<BackgroundTask>(named(HabitNudgeTask.TASK_KEY)) { HabitNudgeTask(get()) }
}
