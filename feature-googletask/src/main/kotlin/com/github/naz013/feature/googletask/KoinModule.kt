package com.github.naz013.feature.googletask

import com.github.naz013.feature.googletask.preview.GoogleTaskPreviewStateAdapter
import com.github.naz013.feature.googletask.preview.PreviewGoogleTaskViewModel
import com.github.naz013.feature.googletask.task.EditGoogleTaskViewModel
import com.github.naz013.feature.googletask.tasklist.EditGoogleTaskListViewModel
import com.github.naz013.feature.googletask.usecase.GoogleTaskListFactory
import com.github.naz013.feature.googletask.usecase.db.DeleteGoogleTaskList
import com.github.naz013.feature.googletask.usecase.db.DeleteGoogleTasks
import com.github.naz013.feature.googletask.usecase.db.GetGoogleTasksByList
import com.github.naz013.feature.googletask.usecase.db.SaveGoogleTaskList
import com.github.naz013.feature.googletask.usecase.db.SaveGoogleTasks
import com.github.naz013.feature.googletask.usecase.remote.DownloadGoogleTaskList
import com.github.naz013.feature.googletask.usecase.remote.DownloadGoogleTasks
import com.github.naz013.feature.googletask.usecase.remote.UploadGoogleTask
import com.github.naz013.feature.googletask.usecase.task.SyncGoogleTasks
import com.github.naz013.feature.googletask.usecase.tasklist.AddNewTaskList
import com.github.naz013.feature.googletask.usecase.tasklist.SyncAllGoogleTaskLists
import com.github.naz013.feature.googletask.usecase.tasklist.SyncGoogleTaskList
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
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
    )
  }
  viewModelOf(::GoogleTasksViewModel)

  factoryOf(::SyncAllGoogleTaskLists)
  factoryOf(::SyncGoogleTaskList)
  factoryOf(::SyncGoogleTasks)
  factoryOf(::AddNewTaskList)
  factoryOf(::SaveGoogleTaskList)
  factoryOf(::SaveGoogleTasks)
  factoryOf(::DeleteGoogleTasks)
  factoryOf(::DeleteGoogleTaskList)
  factoryOf(::DownloadGoogleTasks)
  factoryOf(::DownloadGoogleTaskList)
  factoryOf(::UploadGoogleTask)
  factoryOf(::GetGoogleTasksByList)
  factoryOf(::GoogleTaskListFactory)

  factory { GoogleTaskPreviewStateAdapter(get(), get()) }
}
