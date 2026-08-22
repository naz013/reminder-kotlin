package com.github.naz013.logic.routine

import com.github.naz013.logic.routine.usecase.DeleteRoutineUseCase
import com.github.naz013.logic.routine.usecase.RecordRoutineExecutionUseCase
import com.github.naz013.logic.routine.usecase.ResetRoutineStepsUseCase
import com.github.naz013.logic.routine.usecase.RoutineRecurrenceResetUseCase
import com.github.naz013.logic.routine.usecase.SaveRoutineUseCase
import com.github.naz013.logic.routine.usecase.ToggleRoutinePinUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val logicRoutineModule = module {
  factoryOf(::RoutineDurationCalculator)

  factoryOf(::SaveRoutineUseCase)
  factoryOf(::DeleteRoutineUseCase)
  factoryOf(::ToggleRoutinePinUseCase)
  factoryOf(::ResetRoutineStepsUseCase)
  factoryOf(::RoutineRecurrenceResetUseCase)
  factoryOf(::RecordRoutineExecutionUseCase)

  factory { RoutineConfigImpl(get()) as RoutineConfig }
}
