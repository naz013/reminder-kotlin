package com.github.naz013.ui.tag

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val uiTagModule = module {
  factoryOf(::TagChipStateAdapter)
}
