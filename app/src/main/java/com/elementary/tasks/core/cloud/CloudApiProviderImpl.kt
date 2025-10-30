package com.elementary.tasks.core.cloud

import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.cloudapi.dropbox.DropboxApi
import com.github.naz013.cloudapi.dropbox.DropboxAuthManager
import com.github.naz013.cloudapi.googledrive.GoogleDriveApi
import com.github.naz013.cloudapi.googledrive.GoogleDriveAuthManager
import com.github.naz013.sync.CloudApiProvider

class CloudApiProviderImpl(
  private val googleDriveAuthManager: GoogleDriveAuthManager,
  private val dropboxAuthManager: DropboxAuthManager,
  private val googleDriveApi: GoogleDriveApi,
  private val dropboxApi: DropboxApi
) : CloudApiProvider {

  override fun getAllowedCloudApis(): List<CloudFileApi> {
    return listOfNotNull(
      googleDriveApi.takeIf { googleDriveAuthManager.isAuthorized() },
      dropboxApi.takeIf { dropboxAuthManager.isAuthorized() }
    )
  }
}
