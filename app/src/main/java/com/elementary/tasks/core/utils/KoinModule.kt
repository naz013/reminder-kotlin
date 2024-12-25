package com.elementary.tasks.core.utils

import com.elementary.tasks.core.utils.io.UriHelper
import com.github.naz013.ui.common.font.FontApi
import org.koin.dsl.module

val newUtilsModule = module {
  single { ImageLoader(get()) }
  factory { UriHelper(get()) }
  factory { EventImportProcessor(get(), get(), get(), get(), get(), get()) }

  factory { GeocoderTask(get(), get()) }

  factory { FontApiImpl(get()) as FontApi }
}
