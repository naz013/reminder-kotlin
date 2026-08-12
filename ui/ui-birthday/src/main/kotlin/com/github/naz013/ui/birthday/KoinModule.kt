package com.github.naz013.ui.birthday

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val uiBirthdayModule =
  module {
    factoryOf(::UiBirthdayEditAdapter)
    factoryOf(::UiBirthdayListAdapter)
    factoryOf(::UiBirthdayPreviewAdapter)
    factoryOf(::UiBirthdayDateFormatter)
  }
