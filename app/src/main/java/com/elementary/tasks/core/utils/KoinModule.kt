package com.elementary.tasks.core.utils

import org.koin.dsl.module

val newUtilsModule = module {
  single { ImageLoader(get()) }
  factory { EventImportProcessor(get(), get(), get(), get()) }
}
