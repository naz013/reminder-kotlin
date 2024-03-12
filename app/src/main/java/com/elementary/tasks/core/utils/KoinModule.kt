package com.elementary.tasks.core.utils

import com.elementary.tasks.core.utils.io.UriHelper
import org.koin.dsl.module

val newUtilsModule = module {
  single { ImageLoader(get()) }
  single { UriHelper(get()) }
  factory { EventImportProcessor(get(), get(), get(), get()) }

  factory { GeocoderTask(get(), get()) }
}
