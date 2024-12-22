package com.elementary.tasks.core.cloud

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.cloudapi.CloudKeysStorage

class CloudKeysStorageImpl(
  private val prefs: Prefs
) : CloudKeysStorage {

  override fun getGoogleDriveUserName(): String {
    return prefs.driveUser
  }

  override fun saveGoogleDriveUserName(name: String) {
    prefs.driveUser = name
  }

  override fun getDropboxToken(): String {
    return prefs.dropboxToken
  }

  override fun saveDropboxToken(token: String) {
    prefs.dropboxToken = token
  }

  override fun getGoogleTasksUserName(): String {
    return prefs.tasksUser
  }

  override fun saveGoogleTasksUserName(name: String) {
    prefs.tasksUser = name
  }
}
