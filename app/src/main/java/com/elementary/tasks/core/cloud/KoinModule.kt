package com.elementary.tasks.core.cloud

import com.github.naz013.sync.CloudApiProvider
import com.github.naz013.sync.SyncDataConverter
import com.github.naz013.sync.SyncSettings
import org.koin.dsl.module

val cloudModule = module {
  factory { SyncDataConverterImpl() as SyncDataConverter }
  factory { SyncSettingsImpl() as SyncSettings }
  factory { CloudApiProviderImpl(get(), get(), get(), get()) as CloudApiProvider }
}
