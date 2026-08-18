package com.github.naz013.datecalc

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val dateTimeCalculationsModule = module {
  factoryOf<BirthdayDateCalculator>(::BirthdayDateCalculatorImpl)
  factoryOf<RecurrenceCalculator>(::RecurrenceCalculatorImpl)
  factoryOf(::DateValidator)
  factoryOf<NowDateTimeProvider>(::NowDateTimeProviderImpl)
  factoryOf(::DateTimeManager)
}
