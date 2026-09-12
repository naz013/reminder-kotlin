package com.github.naz013.wearsync

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val wearSyncModule = module {
  factoryOf(::WearReminderSummaryMapper)
  factory { WearSyncGatewayImpl(get(), get()) as WearSyncGateway }
  factoryOf(::WearReminderSyncCoordinator)
}
