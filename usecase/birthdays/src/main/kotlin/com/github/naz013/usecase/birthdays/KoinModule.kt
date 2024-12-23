package com.github.naz013.usecase.birthdays

import org.koin.dsl.module

val birthdaysUseCaseModule = module {
  factory { GetAllBirthdaysUseCase(get()) }
  factory { GetBirthdaysByDayMonthUseCase(get()) }
}
