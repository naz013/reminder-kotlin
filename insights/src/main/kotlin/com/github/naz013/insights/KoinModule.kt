package com.github.naz013.insights

import com.github.naz013.insights.compose.InsightsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val insightsModule = module {
  viewModelOf(::InsightsViewModel)
}
