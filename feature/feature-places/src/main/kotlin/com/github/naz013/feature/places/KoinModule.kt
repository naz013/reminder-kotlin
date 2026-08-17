package com.github.naz013.feature.places

import com.github.naz013.feature.places.create.EditPlaceViewModel
import com.github.naz013.feature.places.list.PlacesViewModel
import com.github.naz013.feature.places.usecase.DeletePlaceUseCase
import com.github.naz013.feature.places.usecase.SavePlaceUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featurePlacesModule = module {
  factoryOf(::DeletePlaceUseCase)
  factoryOf(::SavePlaceUseCase)

  viewModel { (key: PlacesNavKey.Edit) ->
    EditPlaceViewModel(key, get(), get(), get(), get(), get(), get(), get())
  }
  viewModelOf(::PlacesViewModel)
}
