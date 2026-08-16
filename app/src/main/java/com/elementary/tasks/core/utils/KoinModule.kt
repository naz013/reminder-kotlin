package com.elementary.tasks.core.utils

import com.github.naz013.feature.reminder.util.UriHelper
import com.github.naz013.common.ContextProvider
import com.github.naz013.ui.notification.settings.VibrationPlayer
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val newUtilsModule = module {
  singleOf(::ImageLoader)
  factoryOf(::UriHelper)
  factoryOf(::GeocoderTask)
  factory { VibrationPlayer(get<ContextProvider>().context) }
}
