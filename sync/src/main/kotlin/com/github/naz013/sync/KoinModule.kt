package com.github.naz013.sync

import com.github.naz013.sync.local.DataTypeRepositoryCallerFactory
import com.github.naz013.sync.usecase.CreateCloudFileUseCase
import com.github.naz013.sync.usecase.CreateRemoteFileMetadataUseCase
import com.github.naz013.sync.usecase.DownloadSingleUseCase
import com.github.naz013.sync.usecase.FindNewestCloudApiSource
import com.github.naz013.sync.usecase.GetAllowedDataTypesUseCase
import com.github.naz013.sync.usecase.GetLocalUuIdUseCase
import com.github.naz013.sync.usecase.UploadSingleUseCase
import org.koin.dsl.module

val syncApiModule = module {
  factory { SyncApiImpl(get(), get(), get(), get(), get()) as SyncApi }

  factory { DataTypeRepositoryCallerFactory(get(), get(), get(), get(), get()) }

  factory { GetAllowedDataTypesUseCase(get()) }
  factory { CreateRemoteFileMetadataUseCase(get()) }
  factory { CreateCloudFileUseCase(get(), get()) }
  factory { GetLocalUuIdUseCase() }
  factory { UploadSingleUseCase(get(), get(), get(), get(), get(), get()) }

  factory { FindNewestCloudApiSource(get(), get()) }
  factory { DownloadSingleUseCase(get(), get(), get(), get(), get()) }
}
