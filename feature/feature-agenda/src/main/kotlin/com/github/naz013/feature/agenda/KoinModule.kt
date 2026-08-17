package com.github.naz013.feature.agenda

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureAgendaModule = module {
  viewModelOf(::AgendaViewModel)
}
