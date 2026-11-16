package com.github.naz013.feature.search

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureSearchModule = module {
  viewModelOf(::SearchViewModel)
}
