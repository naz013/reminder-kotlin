package com.elementary.tasks.simplemap

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val simpleMapKoinModule = module {
  factoryOf(::MapStyle)

  viewModel { (mapParams: MapParams) ->
    SimpleMapViewViewModel(mapParams, get(), get(), get(), get(), get(), get(), get(), get())
  }
}
