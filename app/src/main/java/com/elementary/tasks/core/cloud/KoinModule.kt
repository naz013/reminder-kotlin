package com.elementary.tasks.core.cloud

import com.elementary.tasks.core.cloud.usecase.GetWorkerTagUseCase
import com.elementary.tasks.core.cloud.usecase.ScheduleBackgroundWorkUseCase
import com.elementary.tasks.core.cloud.worker.DeleteTask
import com.elementary.tasks.core.cloud.worker.ForceUploadTask
import com.elementary.tasks.core.cloud.worker.SyncTask
import com.elementary.tasks.core.cloud.worker.UploadTask
import com.elementary.tasks.module.sync.SyncDataConverterImpl
import com.github.naz013.repository.TagSyncTrigger
import com.github.naz013.sync.CloudApiProvider
import com.github.naz013.sync.DataPostProcessor
import com.github.naz013.sync.FileCacheProvider
import com.github.naz013.sync.SyncDataConverter
import com.github.naz013.sync.SyncSettings
import com.github.naz013.workapi.BackgroundTask
import org.koin.core.qualifier.named
import org.koin.dsl.module

val cloudModule = module {
  factory { SyncSettingsImpl(get()) as SyncSettings }
  factory { CloudApiProviderImpl(get(), get(), get(), get()) as CloudApiProvider }
  factory { DataPostProcessorImpl(get(), get(), get(), get()) as DataPostProcessor }
  factory { FileCacheProviderImpl(get()) as FileCacheProvider }
  single { TagSyncTriggerImpl(get()) as TagSyncTrigger }

  factory { GetWorkerTagUseCase() }
  factory { ScheduleBackgroundWorkUseCase(get(), get(), get(), get()) }

  factory<BackgroundTask>(named(DeleteTask.TASK_KEY)) { DeleteTask(get()) }
  factory<BackgroundTask>(named(ForceUploadTask.TASK_KEY)) { ForceUploadTask(get()) }
  factory<BackgroundTask>(named(SyncTask.TASK_KEY)) { SyncTask(get()) }
  factory<BackgroundTask>(named(UploadTask.TASK_KEY)) { UploadTask(get()) }
}
