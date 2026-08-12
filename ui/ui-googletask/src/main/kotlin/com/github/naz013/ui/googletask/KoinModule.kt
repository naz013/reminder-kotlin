package com.github.naz013.ui.googletask

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val uiGoogleTaskModule = module {
  factoryOf(::GoogleTaskItemStateAdapter)
}
