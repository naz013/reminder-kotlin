package com.elementary.tasks.core.cloud

import com.github.naz013.repository.TagSyncTrigger
import com.github.naz013.sync.CloudApiProvider
import com.github.naz013.sync.DataPostProcessor
import com.github.naz013.sync.FileCacheProvider
import com.github.naz013.sync.IsProUserUseCase
import com.github.naz013.sync.SyncSettings
import org.koin.dsl.module

val cloudModule = module {
  factory { SyncSettingsImpl(get()) as SyncSettings }
  factory { CloudApiProviderImpl(get(), get(), get(), get()) as CloudApiProvider }
  factory { DataPostProcessorImpl(get(), get(), get(), get()) as DataPostProcessor }
  factory { FileCacheProviderImpl(get()) as FileCacheProvider }
  single { TagSyncTriggerImpl(get()) as TagSyncTrigger }
  factory { IsProUserUseCaseImpl(get()) as IsProUserUseCase }
}
