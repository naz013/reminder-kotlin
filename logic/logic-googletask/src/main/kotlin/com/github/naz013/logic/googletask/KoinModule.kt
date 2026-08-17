package com.github.naz013.logic.googletask

import com.github.naz013.logic.googletask.usecase.GoogleTaskListFactory
import com.github.naz013.logic.googletask.usecase.SyncAllGoogleTaskListsUseCase
import com.github.naz013.logic.googletask.usecase.db.DeleteGoogleTaskList
import com.github.naz013.logic.googletask.usecase.remote.DownloadGoogleTaskList
import com.github.naz013.logic.googletask.usecase.remote.DownloadGoogleTasks
import com.github.naz013.logic.googletask.usecase.remote.UploadGoogleTask
import com.github.naz013.logic.googletask.usecase.task.SyncGoogleTasks
import com.github.naz013.logic.googletask.usecase.tasklist.AddNewTaskList
import com.github.naz013.logic.googletask.usecase.tasklist.SyncAllGoogleTaskListsUseCaseImpl
import com.github.naz013.logic.googletask.usecase.tasklist.SyncGoogleTaskList
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val logicGoogleTaskModule = module {
  factory<SyncAllGoogleTaskListsUseCase> { SyncAllGoogleTaskListsUseCaseImpl(get(), get(), get(), get(), get(), get()) }

  factoryOf(::SyncGoogleTaskList)
  factoryOf(::SyncGoogleTasks)
  factoryOf(::AddNewTaskList)
  factoryOf(::DeleteGoogleTaskList)
  factoryOf(::DownloadGoogleTasks)
  factoryOf(::DownloadGoogleTaskList)
  factoryOf(::UploadGoogleTask)
  factoryOf(::GoogleTaskListFactory)
}
