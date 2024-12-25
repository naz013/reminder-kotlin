package com.github.naz013.usecase.reminders

import org.koin.dsl.module

val remindersUseCaseModule = module {
  factory { GetActiveRemindersUseCase(get()) }
  factory { GetActiveRemindersWithoutGpsUseCase(get()) }
}
