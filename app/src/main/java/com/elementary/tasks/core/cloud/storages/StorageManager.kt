package com.elementary.tasks.core.cloud.storages

import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.cloudapi.dropbox.DropboxApi
import com.github.naz013.cloudapi.dropbox.DropboxAuthManager
import com.github.naz013.cloudapi.googledrive.GoogleDriveApi
import com.github.naz013.cloudapi.googledrive.GoogleDriveAuthManager

class StorageManager(
  private val dropbox: DropboxApi,
  private val dropboxAuthManager: DropboxAuthManager,
  private val googleDriveApi: GoogleDriveApi,
  private val googleDriveAuthManager: GoogleDriveAuthManager
) {

  fun availableStorageList(): List<CloudFileApi> {
    return listOfNotNull(
      googleDriveApi.takeIf { googleDriveAuthManager.isAuthorized() },
      dropbox.takeIf { dropboxAuthManager.isAuthorized() }
    )
  }
}
