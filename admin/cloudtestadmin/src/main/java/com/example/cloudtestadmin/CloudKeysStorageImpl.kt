package com.example.cloudtestadmin

import android.content.Context
import androidx.core.content.edit
import com.github.naz013.cloudapi.CloudKeysStorage

class CloudKeysStorageImpl(
  private val context: Context
) : CloudKeysStorage {

  private val sharedPreferences by lazy {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  override fun getGoogleDriveUserName(): String {
    return sharedPreferences.getString(KEY_DRIVE_USER, "") ?: ""
  }

  override fun saveGoogleDriveUserName(name: String) {
    sharedPreferences.edit {
      putString(KEY_DRIVE_USER, name)
    }
  }

  override fun getDropboxToken(): String {
    return sharedPreferences.getString(KEY_DROPBOX_TOKEN, "") ?: ""
  }

  override fun saveDropboxToken(token: String) {
    sharedPreferences.edit {
      putString(KEY_DROPBOX_TOKEN, token)
    }
  }

  override fun getGoogleTasksUserName(): String {
    return ""
  }

  override fun saveGoogleTasksUserName(name: String) {
    // No-op
  }

  companion object {
    private const val PREFS_NAME = "cloud_test_admin_prefs"
    private const val KEY_DRIVE_USER = "drive_user"
    private const val KEY_DROPBOX_TOKEN = "dropbox_token"
  }
}
