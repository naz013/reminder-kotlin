package com.elementary.tasks.core.os

import org.koin.dsl.module

val osModule = module {
  factory { ContextSwitcher(get()) }
}
