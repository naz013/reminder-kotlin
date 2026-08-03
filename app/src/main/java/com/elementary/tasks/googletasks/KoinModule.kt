package com.elementary.tasks.googletasks

import com.elementary.tasks.googletasks.list.TaskListViewModel
import com.elementary.tasks.googletasks.preview.PreviewGoogleTaskViewModel
import com.elementary.tasks.googletasks.task.EditGoogleTaskViewModel
import com.elementary.tasks.googletasks.tasklist.EditGoogleTaskListViewModel
import com.elementary.tasks.googletasks.usecase.GoogleTaskListFactory
import com.elementary.tasks.googletasks.usecase.db.DeleteGoogleTaskList
import com.elementary.tasks.googletasks.usecase.db.DeleteGoogleTasks
import com.elementary.tasks.googletasks.usecase.db.GetGoogleTasksByList
import com.elementary.tasks.googletasks.usecase.db.SaveGoogleTaskList
import com.elementary.tasks.googletasks.usecase.db.SaveGoogleTasks
import com.elementary.tasks.googletasks.usecase.remote.DownloadGoogleTaskList
import com.elementary.tasks.googletasks.usecase.remote.DownloadGoogleTasks
import com.elementary.tasks.googletasks.usecase.remote.UploadGoogleTask
import com.elementary.tasks.googletasks.usecase.task.SyncGoogleTasks
import com.elementary.tasks.googletasks.usecase.tasklist.AddNewTaskList
import com.elementary.tasks.googletasks.usecase.tasklist.SyncAllGoogleTaskLists
import com.elementary.tasks.googletasks.usecase.tasklist.SyncGoogleTaskList
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val googleTaskModule = module {
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
}
