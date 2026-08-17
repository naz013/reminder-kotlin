package com.github.naz013.ui.map

import com.github.naz013.ui.map.place.UiPlaceListAdapter
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val uiMapModule = module {
  factoryOf(::MapStyle)
  factoryOf(::GeocoderTask)
  factoryOf(::UiPlaceListAdapter)

  viewModel { (mapParams: MapParams) ->
    SimpleMapViewViewModel(mapParams, get(), get(), get(), get(), get(), get(), get(), get(), get())
  }
}
