package com.github.naz013.logic.search

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val logicSearchModule = module {
  factoryOf(::GlobalSearchUseCase)
  factoryOf(::SaveRecentSearchQueryUseCase)
  factoryOf(::GetRecentSearchQueriesUseCase)
}
