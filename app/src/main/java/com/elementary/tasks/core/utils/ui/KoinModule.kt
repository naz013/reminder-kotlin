package com.elementary.tasks.core.utils.ui

import com.elementary.tasks.whatsnew.WhatsNewManager
import org.koin.dsl.module

val uiUtilsModule = module {
  single { WhatsNewManager(get(), get()) }
}
