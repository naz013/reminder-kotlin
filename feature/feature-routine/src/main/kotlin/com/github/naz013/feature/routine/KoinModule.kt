package com.github.naz013.feature.routine

import com.github.naz013.feature.routine.edit.RoutineEditViewModel
import com.github.naz013.feature.routine.execution.RoutineExecutionViewModel
import com.github.naz013.feature.routine.list.RoutinesListViewModel
import com.github.naz013.feature.routine.preview.RoutinePreviewViewModel
import com.github.naz013.workapi.BackgroundTask
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val routineModule = module {
  factory<BackgroundTask>(named(RoutineRecurrenceResetTask.TASK_KEY)) {
    RoutineRecurrenceResetTask(get(), get())
  }

  viewModelOf(::RoutinesListViewModel)

  viewModel { (id: String?) ->
    RoutineEditViewModel(id, get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get())
  }

  viewModel { (id: String) ->
    RoutinePreviewViewModel(id, get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get())
  }

  viewModel { (id: String) ->
    RoutineExecutionViewModel(id, get(), get(), get(), get(), get(), get())
  }
}
