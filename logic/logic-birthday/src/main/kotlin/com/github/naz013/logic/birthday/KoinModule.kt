package com.github.naz013.logic.birthday

import com.github.naz013.workapi.BackgroundTask
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val logicBirthdayModule =
  module {
    factoryOf(::SaveBirthdayUseCase)
    factoryOf(::DeleteBirthdayUseCase)
    factoryOf(::BirthdaySmartListPredicate)
    factoryOf(::CalculateBirthdayOccurrencesUseCase)

    factory<BackgroundTask>(named(CalculateBirthdayOccurrencesTask.TASK_KEY)) {
      CalculateBirthdayOccurrencesTask(get())
    }
  }
