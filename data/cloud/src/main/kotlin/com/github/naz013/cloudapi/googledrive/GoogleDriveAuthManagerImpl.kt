package com.github.naz013.cloudapi.googledrive

import android.content.Context
import com.github.naz013.cloudapi.CloudKeysStorage
import com.github.naz013.logging.Logger
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.services.drive.DriveScopes

internal class GoogleDriveAuthManagerImpl(
  private val context: Context,
  private val cloudKeysStorage: CloudKeysStorage
) : GoogleDriveAuthManager {

  override fun isAuthorized(): Boolean {
    val userName = cloudKeysStorage.getGoogleDriveUserName()
    return (userName.isNotEmpty() && userName.matches(".*@.*".toRegex())).also {
      Logger.i(TAG, "Google Drive is authorized: $it")
    }
  }

  override fun hasGooglePlayServices(): Boolean {
    val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
    return (resultCode == ConnectionResult.SUCCESS).also {
      Logger.d(TAG, "Google Play Services available: $it")
    }
  }

  override fun getUserName(): String {
    return cloudKeysStorage.getGoogleDriveUserName()
  }

  override fun removeUserName() {
    cloudKeysStorage.saveGoogleDriveUserName("")
    Logger.d(TAG, "Google Drive user name removed")
  }

  override fun saveUserName(name: String) {
    cloudKeysStorage.saveGoogleDriveUserName(name)
    Logger.d(TAG, "Google Drive user name saved")
  }

  override fun getScopes(): List<String> {
    return listOf(
      DriveScopes.DRIVE_APPDATA,
      DriveScopes.DRIVE_FILE
    )
  }

  companion object {
    private const val TAG = "GoogleDriveAuthManager"
  }
}
