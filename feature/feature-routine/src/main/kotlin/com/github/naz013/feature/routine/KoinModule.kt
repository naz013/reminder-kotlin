package com.github.naz013.feature.routine

import com.github.naz013.feature.routine.edit.RoutineEditViewModel
import com.github.naz013.feature.routine.list.RoutinesListViewModel
import com.github.naz013.feature.routine.preview.RoutinePreviewViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val routineModule = module {
  viewModelOf(::RoutinesListViewModel)

  viewModel { (id: String?) ->
    RoutineEditViewModel(id, get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get())
  }

  viewModel { (id: String) ->
    RoutinePreviewViewModel(id, get(), get(), get(), get(), get(), get(), get(), get(), get(), get())
  }
}
