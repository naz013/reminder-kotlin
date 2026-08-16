package com.elementary.tasks.reminder

import com.github.naz013.logic.reminder.scheduling.EventDateTimeCalculatorV2
import com.github.naz013.logic.reminder.usecase.CompleteReminderUseCase
import com.github.naz013.logic.reminder.usecase.ResumeReminderUseCase
import com.github.naz013.logic.reminder.usecase.SkipReminderUseCase
import com.github.naz013.logic.reminder.usecase.ToggleReminderStateUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val reminderModule =
  module {
    factoryOf(::ResumeReminderUseCase)

    factoryOf(::CompleteReminderUseCase)
    factoryOf(::SkipReminderUseCase)

    factoryOf(::ToggleReminderStateUseCase)

    factory { EventDateTimeCalculatorV2(get(), get()) }
  }
