package com.elementary.tasks.core.services

import com.elementary.tasks.core.services.usecase.CheckLocationReminderUseCase
import com.elementary.tasks.core.services.usecase.PlaceDistanceCalculator
import org.koin.dsl.module

val servicesModule =
  module {
    factory { PlaceDistanceCalculator() }
    factory { CheckLocationReminderUseCase(get(), get(), get(), get(), get(), get(), get()) }
  }
