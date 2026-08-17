package com.github.naz013.ui.agenda

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val uiAgendaModule = module {
  factoryOf(::UiAgendaItemAdapter)
}
