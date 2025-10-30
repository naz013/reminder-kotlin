package com.elementary.tasks.places

import com.elementary.tasks.places.create.EditPlaceViewModel
import com.elementary.tasks.places.list.PlacesViewModel
import com.elementary.tasks.places.usecase.DeletePlaceUseCase
import com.elementary.tasks.places.usecase.SavePlaceUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val placeKoinModule = module {
  factory { DeletePlaceUseCase(get(), get()) }
  factory { SavePlaceUseCase(get(), get()) }

  viewModel { (id: String) ->
    EditPlaceViewModel(id, get(), get(), get(), get(), get(), get(), get(), get())
  }
  viewModel { PlacesViewModel(get(), get(), get(), get(), get()) }
}
