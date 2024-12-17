package com.elementary.tasks.core.services

import com.elementary.tasks.core.services.usecase.CheckLocationReminderUseCase
import org.koin.dsl.module

val servicesModule = module {
  factory { CheckLocationReminderUseCase(get(), get(), get(), get()) }
}
