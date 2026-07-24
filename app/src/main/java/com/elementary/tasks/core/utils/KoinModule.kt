package com.elementary.tasks.core.utils

import com.elementary.tasks.core.utils.io.UriHelper
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val newUtilsModule = module {
  singleOf(::ImageLoader)
  factoryOf(::UriHelper)
  factoryOf(::GeocoderTask)
}
