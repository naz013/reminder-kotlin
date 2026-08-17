package com.github.naz013.feature.googletask

import com.github.naz013.feature.googletask.preview.GoogleTaskPreviewStateAdapter
import com.github.naz013.feature.googletask.preview.PreviewGoogleTaskViewModel
import com.github.naz013.feature.googletask.task.EditGoogleTaskViewModel
import com.github.naz013.feature.googletask.tasklist.EditGoogleTaskListViewModel
import com.github.naz013.feature.googletask.work.SaveNewTaskTask
import com.github.naz013.feature.googletask.work.UpdateTaskTask
import com.github.naz013.workapi.BackgroundTask
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val featureGoogleTaskModule = module {
  viewModel { (listId: String?) ->
    EditGoogleTaskListViewModel(
      listId,
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get()
    )
  }
  viewModel { (id: String?, listId: String) ->
    EditGoogleTaskViewModel(
      id,
      listId,
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
    )
  }
  viewModel { (id: String) ->
    PreviewGoogleTaskViewModel(
      id,
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
    )
  }
  viewModel { (listId: String) ->
    TaskListViewModel(
      listId,
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
    )
  }
  viewModelOf(::GoogleTasksViewModel)

  factory { GoogleTaskPreviewStateAdapter(get(), get()) }

  factory<BackgroundTask>(named(SaveNewTaskTask.TASK_KEY)) { SaveNewTaskTask(get(), get()) }
  factory<BackgroundTask>(named(UpdateTaskTask.TASK_KEY)) { UpdateTaskTask(get(), get()) }
}
