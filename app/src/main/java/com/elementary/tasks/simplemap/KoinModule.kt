package com.elementary.tasks.simplemap

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val simpleMapKoinModule =
  module {
    viewModel { (mapParams: MapParams) ->
      MapViewModel(mapParams, get(), get(), get(), get(), get(), get())
    }
  }
