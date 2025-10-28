package com.elementary.tasks.core.cloud

import com.elementary.tasks.core.cloud.usecase.GetWorkerTagUseCase
import com.elementary.tasks.core.cloud.usecase.ScheduleUploadUseCase
import com.github.naz013.sync.CloudApiProvider
import com.github.naz013.sync.SyncDataConverter
import com.github.naz013.sync.SyncSettings
import org.koin.dsl.module

val cloudModule = module {
  factory { SyncDataConverterImpl() as SyncDataConverter }
  factory { SyncSettingsImpl(get()) as SyncSettings }
  factory { CloudApiProviderImpl(get(), get(), get(), get()) as CloudApiProvider }

  factory { GetWorkerTagUseCase() }
  factory { ScheduleUploadUseCase(get(), get()) }
}
