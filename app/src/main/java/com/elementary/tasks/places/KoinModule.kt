package com.elementary.tasks.places

import com.elementary.tasks.places.create.EditPlaceViewModel
import com.elementary.tasks.places.list.PlacesViewModel
import com.elementary.tasks.places.usecase.DeletePlaceUseCase
import com.elementary.tasks.places.usecase.SavePlaceUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val placeKoinModule = module {
  factoryOf(::DeletePlaceUseCase)
  factoryOf(::SavePlaceUseCase)

  viewModel { (key: PlacesNavKey.Edit) ->
    EditPlaceViewModel(key, get(), get(), get(), get(), get(), get(), get())
  }
  viewModelOf(::PlacesViewModel)
}
