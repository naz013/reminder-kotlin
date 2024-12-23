package com.elementary.tasks.core.os

import org.koin.dsl.module

val osModule = module {

  single { IntentDataHolder() }

  factory { ContextSwitcher(get()) }
}
