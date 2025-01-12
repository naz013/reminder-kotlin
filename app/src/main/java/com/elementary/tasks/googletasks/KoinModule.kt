package com.elementary.tasks.googletasks

import android.os.Bundle
import com.elementary.tasks.googletasks.list.TaskListViewModel
import com.elementary.tasks.googletasks.preview.PreviewGoogleTaskViewModel
import com.elementary.tasks.googletasks.task.EditGoogleTaskViewModel
import com.elementary.tasks.googletasks.tasklist.GoogleTaskListViewModel
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
  viewModel { (arguments: Bundle?) ->
    EditGoogleTaskViewModel(
      arguments,
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
    PreviewGoogleTaskViewModel(
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

  factory { SyncAllGoogleTaskLists(get(), get(), get(), get(), get(), get()) }
  factory { SyncGoogleTaskList(get(), get(), get(), get()) }

  factory { SyncGoogleTasks(get(), get(), get(), get(), get()) }

  factory { AddNewTaskList(get(), get(), get()) }

  factory { SaveGoogleTaskList(get()) }
  factory { SaveGoogleTasks(get()) }

  factory { DeleteGoogleTasks(get()) }
  factory { DeleteGoogleTaskList(get(), get(), get()) }

  factory { DownloadGoogleTasks(get()) }
  factory { DownloadGoogleTaskList(get(), get()) }

  factory { UploadGoogleTask(get(), get()) }

  factory { GetGoogleTasksByList(get()) }

  factory { GoogleTaskListFactory() }
}
