package com.elementary.tasks.core.utils

import com.github.naz013.common.ContextProvider
import com.github.naz013.ui.notification.settings.VibrationPlayer
import org.koin.dsl.module

val newUtilsModule = module {
  factory { VibrationPlayer(get<ContextProvider>().context) }
}
