package com.github.naz013.feature.habit

import com.github.naz013.feature.habit.editor.HabitEditorViewModel
import com.github.naz013.feature.habit.list.HabitListViewModel
import com.github.naz013.feature.habit.preview.HabitPreviewViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val habitModule = module {
  viewModelOf(::HabitListViewModel)

  viewModel { (id: String) ->
    HabitPreviewViewModel(id, get(), get(), get(), get(), get(), get())
  }

  viewModel { (id: String?) ->
    HabitEditorViewModel(id, get(), get(), get(), get(), get(), get(), get(), get(), get())
  }
}
