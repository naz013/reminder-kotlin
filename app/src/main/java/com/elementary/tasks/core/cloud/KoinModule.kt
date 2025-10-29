package com.elementary.tasks.core.cloud

import com.elementary.tasks.core.cloud.usecase.GetWorkerTagUseCase
import com.elementary.tasks.core.cloud.usecase.ScheduleBackgroundWorkUseCase
import com.elementary.tasks.core.cloud.worker.DeleteWorker
import com.elementary.tasks.core.cloud.worker.ForceUploadWorker
import com.elementary.tasks.core.cloud.worker.SyncWorker
import com.elementary.tasks.core.cloud.worker.UploadWorker
import com.github.naz013.sync.CloudApiProvider
import com.github.naz013.sync.DataPostProcessor
import com.github.naz013.sync.SyncDataConverter
import com.github.naz013.sync.SyncSettings
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val cloudModule = module {
  factory { SyncDataConverterImpl() as SyncDataConverter }
  factory { SyncSettingsImpl(get()) as SyncSettings }
  factory { CloudApiProviderImpl(get(), get(), get(), get()) as CloudApiProvider }
  factory { DataPostProcessorImpl(get(), get(), get(), get(), get(), get()) as DataPostProcessor }

  factory { GetWorkerTagUseCase() }
  factory { ScheduleBackgroundWorkUseCase(get(), get()) }

  worker { DeleteWorker(get(), get(), get(), get()) }
  worker { ForceUploadWorker(get(), get(), get(), get()) }
  worker { SyncWorker(get(), get(), get(), get()) }
  worker { UploadWorker(get(), get(), get(), get()) }
}
