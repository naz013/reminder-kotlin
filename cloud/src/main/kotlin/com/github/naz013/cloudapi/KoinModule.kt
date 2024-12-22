package com.github.naz013.cloudapi

import com.github.naz013.cloudapi.dropbox.DropboxApi
import com.github.naz013.cloudapi.dropbox.DropboxApiImpl
import com.github.naz013.cloudapi.dropbox.DropboxAuthManager
import com.github.naz013.cloudapi.dropbox.DropboxAuthManagerImpl
import com.github.naz013.cloudapi.googledrive.GoogleDriveApi
import com.github.naz013.cloudapi.googledrive.GoogleDriveApiImpl
import com.github.naz013.cloudapi.googledrive.GoogleDriveAuthManager
import com.github.naz013.cloudapi.googledrive.GoogleDriveAuthManagerImpl
import com.github.naz013.cloudapi.googletasks.GetRandomGoogleTaskListColorUseCase
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.cloudapi.googletasks.GoogleTasksApiImpl
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManagerImpl
import com.github.naz013.cloudapi.googletasks.GoogleTasksModelFactory
import org.koin.dsl.module

val cloudApiModule = module {
  single { GoogleDriveApiImpl(get(), get()) }
  single { get<GoogleDriveApiImpl>() as GoogleDriveApi }
  single { get<GoogleDriveApiImpl>() as CloudFileApi }

  single { DropboxApiImpl(get()) }
  single { get<DropboxApiImpl>() as DropboxApi }
  single { get<DropboxApiImpl>() as CloudFileApi }

  single { GoogleTasksApiImpl(get(), get(), get()) as GoogleTasksApi }

  factory { GoogleDriveAuthManagerImpl(get(), get()) as GoogleDriveAuthManager }
  factory { DropboxAuthManagerImpl(get(), get()) as DropboxAuthManager }
  factory { GoogleTasksAuthManagerImpl(get(), get()) as GoogleTasksAuthManager }

  factory { GetRandomGoogleTaskListColorUseCase() }
  factory { GoogleTasksModelFactory(get()) }
}
