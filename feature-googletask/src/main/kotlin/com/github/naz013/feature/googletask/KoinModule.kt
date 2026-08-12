package com.github.naz013.feature.googletask

import com.github.naz013.feature.googletask.preview.GoogleTaskPreviewStateAdapter
import com.github.naz013.feature.googletask.preview.PreviewGoogleTaskViewModel
import com.github.naz013.feature.googletask.task.EditGoogleTaskViewModel
import com.github.naz013.feature.googletask.tasklist.EditGoogleTaskListViewModel
import com.github.naz013.feature.googletask.usecase.GoogleTaskListFactory
import com.github.naz013.feature.googletask.usecase.SyncAllGoogleTaskListsUseCase
import com.github.naz013.feature.googletask.usecase.db.DeleteGoogleTaskList
import com.github.naz013.feature.googletask.usecase.remote.DownloadGoogleTaskList
import com.github.naz013.feature.googletask.usecase.remote.DownloadGoogleTasks
import com.github.naz013.feature.googletask.usecase.remote.UploadGoogleTask
import com.github.naz013.feature.googletask.usecase.task.SyncGoogleTasks
import com.github.naz013.feature.googletask.usecase.tasklist.AddNewTaskList
import com.github.naz013.feature.googletask.usecase.tasklist.SyncAllGoogleTaskListsUseCaseImpl
import com.github.naz013.feature.googletask.usecase.tasklist.SyncGoogleTaskList
import com.github.naz013.feature.googletask.work.SaveNewTaskTask
import com.github.naz013.feature.googletask.work.UpdateTaskTask
import com.github.naz013.workapi.BackgroundTask
import org.koin.core.module.dsl.factoryOf
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

  factory<SyncAllGoogleTaskListsUseCase> { SyncAllGoogleTaskListsUseCaseImpl(get(), get(), get(), get(), get(), get()) }

  factoryOf(::SyncGoogleTaskList)
  factoryOf(::SyncGoogleTasks)
  factoryOf(::AddNewTaskList)
  factoryOf(::DeleteGoogleTaskList)
  factoryOf(::DownloadGoogleTasks)
  factoryOf(::DownloadGoogleTaskList)
  factoryOf(::UploadGoogleTask)
  factoryOf(::GoogleTaskListFactory)

  factory { GoogleTaskPreviewStateAdapter(get(), get()) }

  factory<BackgroundTask>(named(SaveNewTaskTask.TASK_KEY)) { SaveNewTaskTask(get(), get()) }
  factory<BackgroundTask>(named(UpdateTaskTask.TASK_KEY)) { UpdateTaskTask(get(), get()) }
}
