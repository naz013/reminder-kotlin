package com.elementary.tasks.googletasks

import com.elementary.tasks.googletasks.list.TaskListViewModel
import com.elementary.tasks.googletasks.preview.GoogleTaskPreviewViewModel
import com.elementary.tasks.googletasks.task.GoogleTaskViewModel
import com.elementary.tasks.googletasks.tasklist.GoogleTaskListViewModel
import com.elementary.tasks.googletasks.usecase.GetRandomGoogleTaskListColor
import com.elementary.tasks.googletasks.usecase.GoogleTaskFactory
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
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val googleTaskModule = module {
  viewModel { (listId: String) ->
    GoogleTaskListViewModel(
      listId,
      get(),
      get(),
      get(),
      get(),
      get(),
      get()
    )
  }
  viewModel { (id: String) ->
    GoogleTaskViewModel(
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
      get()
    )
  }
  viewModel { (id: String) ->
    GoogleTaskPreviewViewModel(
      id,
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get()
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
      get()
    )
  }
  viewModel { GoogleTasksViewModel(get(), get(), get(), get(), get(), get(), get()) }

  factory { SyncAllGoogleTaskLists(get(), get(), get(), get(), get()) }
  factory { SyncGoogleTaskList(get(), get(), get(), get()) }

  factory { SyncGoogleTasks(get(), get(), get(), get(), get()) }

  factory { AddNewTaskList(get(), get(), get(), get(), get()) }

  factory { SaveGoogleTaskList(get()) }
  factory { SaveGoogleTasks(get()) }

  factory { DeleteGoogleTasks(get()) }
  factory { DeleteGoogleTaskList(get(), get(), get()) }

  factory { DownloadGoogleTasks(get(), get()) }
  factory { DownloadGoogleTaskList(get(), get()) }

  factory { UploadGoogleTask(get()) }

  factory { GetGoogleTasksByList(get()) }

  factory { GetRandomGoogleTaskListColor() }

  factory { GoogleTaskFactory(get()) }
  factory { GoogleTaskListFactory(get()) }
}
