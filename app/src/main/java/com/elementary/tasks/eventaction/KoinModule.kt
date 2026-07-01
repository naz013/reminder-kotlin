package com.elementary.tasks.eventaction

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val eventActionModule = module {
  factoryOf(::DispatchEventActionUseCase)
  factoryOf(::ResolveReminderEventActionUseCase)
}
