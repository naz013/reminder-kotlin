package com.github.naz013.cloudapi

import com.github.naz013.cloudapi.dropbox.DropboxApi
import com.github.naz013.cloudapi.dropbox.DropboxApiImpl
import com.github.naz013.cloudapi.dropbox.DropboxAuthManager
import com.github.naz013.cloudapi.dropbox.DropboxAuthManagerImpl
import com.github.naz013.cloudapi.googledrive.GoogleDriveApi
import com.github.naz013.cloudapi.googledrive.GoogleDriveApiImpl
import com.github.naz013.cloudapi.googledrive.GoogleDriveAuthManager
import com.github.naz013.cloudapi.googledrive.GoogleDriveAuthManagerImpl
import org.koin.dsl.module

val cloudApiModule = module {
  single { GoogleDriveApiImpl(get(), get()) }
  single { get<GoogleDriveApiImpl>() as GoogleDriveApi }
  single { get<GoogleDriveApiImpl>() as CloudFileApi }

  single { DropboxApiImpl(get()) }
  single { get<DropboxApiImpl>() as DropboxApi }
  single { get<DropboxApiImpl>() as CloudFileApi }

  factory { GoogleDriveAuthManagerImpl(get(), get()) as GoogleDriveAuthManager }
  factory { DropboxAuthManagerImpl(get(), get()) as DropboxAuthManager }
}
