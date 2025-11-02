package com.github.naz013.sync

import com.github.naz013.sync.cache.SyncApiSessionCache
import com.github.naz013.sync.images.CacheFileUseCase
import com.github.naz013.sync.images.DownloadNoteFilesUseCase
import com.github.naz013.sync.images.PostProcessNoteV3UseCase
import com.github.naz013.sync.images.PostProcessOldNoteUseCase
import com.github.naz013.sync.images.UploadFilesUseCase
import com.github.naz013.sync.local.DataTypeRepositoryCallerFactory
import com.github.naz013.sync.settings.UploadSettingsUseCase
import com.github.naz013.sync.usecase.CreateCloudFileUseCase
import com.github.naz013.sync.usecase.CreateRemoteFileMetadataUseCase
import com.github.naz013.sync.usecase.delete.DeleteDataTypeUseCase
import com.github.naz013.sync.usecase.delete.DeleteSingleUseCase
import com.github.naz013.sync.usecase.FindAllFilesToDeleteUseCase
import com.github.naz013.sync.usecase.FindAllFilesToDownloadUseCase
import com.github.naz013.sync.usecase.FindNewestCloudApiSourceUseCase
import com.github.naz013.sync.usecase.GetAllowedCloudApisUseCase
import com.github.naz013.sync.usecase.GetAllowedDataTypesUseCase
import com.github.naz013.sync.usecase.download.GetClassByDataTypeUseCase
import com.github.naz013.sync.usecase.GetCloudFileNameUseCase
import com.github.naz013.sync.usecase.GetLocalUuIdUseCase
import com.github.naz013.sync.usecase.HasAnyCloudApiUseCase
import com.github.naz013.sync.usecase.upload.UploadDataTypeUseCase
import com.github.naz013.sync.usecase.download.DownloadCloudFileUseCase
import com.github.naz013.sync.usecase.download.DownloadLegacyFilesUseCase
import com.github.naz013.sync.usecase.download.DownloadSingleUseCase
import com.github.naz013.sync.usecase.download.DownloadUseCase
import com.github.naz013.sync.usecase.download.PostProcessDownloadedFileUseCase
import com.github.naz013.sync.usecase.upload.PreProcessUploadingFileUseCase
import com.github.naz013.sync.usecase.upload.UploadSingleUseCase
import org.koin.dsl.module

val syncApiModule = module {
  factory { SyncApiImpl(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) as SyncApi }

  factory { DataTypeRepositoryCallerFactory(get(), get(), get(), get(), get(), get()) }

  factory { GetAllowedDataTypesUseCase() }
  factory { CreateRemoteFileMetadataUseCase(get()) }
  factory { CreateCloudFileUseCase(get(), get()) }
  factory { GetLocalUuIdUseCase() }
  factory { UploadSingleUseCase(get(), get(), get(), get(), get(), get(), get()) }

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

  factory { GetClassByDataTypeUseCase() }

  factory { GetAllowedCloudApisUseCase(get(), get()) }

  single { SyncApiSessionCache() }

  factory { PostProcessDownloadedFileUseCase(get(), get()) }
  factory { PostProcessNoteV3UseCase(get()) }
  factory { DownloadNoteFilesUseCase(get()) }
  factory { PostProcessOldNoteUseCase(get()) }

  factory { DownloadCloudFileUseCase(get(), get(), get()) }
  factory { DownloadLegacyFilesUseCase(get(), get(), get(), get(), get()) }

  factory { UploadFilesUseCase(get(), get()) }
  factory { PreProcessUploadingFileUseCase(get()) }
  factory { CacheFileUseCase(get()) }
}
