package com.github.naz013.sync

import com.github.naz013.sync.local.DataTypeRepositoryCallerFactory
import com.github.naz013.sync.settings.UploadSettingsUseCase
import com.github.naz013.sync.usecase.CreateCloudFileUseCase
import com.github.naz013.sync.usecase.CreateRemoteFileMetadataUseCase
import com.github.naz013.sync.usecase.DeleteDataTypeUseCase
import com.github.naz013.sync.usecase.DeleteSingleUseCase
import com.github.naz013.sync.usecase.DownloadSingleUseCase
import com.github.naz013.sync.usecase.DownloadUseCase
import com.github.naz013.sync.usecase.FindAllFilesToDeleteUseCase
import com.github.naz013.sync.usecase.FindAllFilesToDownloadUseCase
import com.github.naz013.sync.usecase.FindNewestCloudApiSourceUseCase
import com.github.naz013.sync.usecase.GetAllowedDataTypesUseCase
import com.github.naz013.sync.usecase.GetCloudFileNameUseCase
import com.github.naz013.sync.usecase.GetLocalUuIdUseCase
import com.github.naz013.sync.usecase.HasAnyCloudApiUseCase
import com.github.naz013.sync.usecase.UploadDataTypeUseCase
import com.github.naz013.sync.usecase.UploadSingleUseCase
import org.koin.dsl.module

val syncApiModule = module {
  factory { SyncApiImpl(get(), get(), get(), get(), get(), get(), get(), get(), get()) as SyncApi }

  factory { DataTypeRepositoryCallerFactory(get(), get(), get(), get(), get()) }

  factory { GetAllowedDataTypesUseCase() }
  factory { CreateRemoteFileMetadataUseCase(get()) }
  factory { CreateCloudFileUseCase(get(), get()) }
  factory { GetLocalUuIdUseCase() }
  factory { UploadSingleUseCase(get(), get(), get(), get(), get(), get()) }

  factory { FindNewestCloudApiSourceUseCase(get(), get()) }
  factory { DownloadSingleUseCase(get(), get(), get(), get(), get(), get()) }
  factory { FindAllFilesToDownloadUseCase(get(), get()) }
  factory { DownloadUseCase(get(), get(), get(), get(), get(), get(), get()) }

  factory { GetCloudFileNameUseCase() }
  factory { FindAllFilesToDeleteUseCase(get()) }
  factory { DeleteSingleUseCase(get(), get(), get()) }
  factory { DeleteDataTypeUseCase(get(), get()) }

  factory { UploadDataTypeUseCase(get(), get(), get()) }
  factory { UploadSettingsUseCase(get(), get(), get(), get()) }

  factory { HasAnyCloudApiUseCase(get()) }
}
