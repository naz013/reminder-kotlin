package com.elementary.tasks.core.data.factory

import org.koin.dsl.module

val dataFactory = module {
  single { GoogleTaskFactory(get()) }
  single { GoogleTaskListFactory(get()) }
}
