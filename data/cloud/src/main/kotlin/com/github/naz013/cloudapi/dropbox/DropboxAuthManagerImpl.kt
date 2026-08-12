package com.github.naz013.cloudapi.dropbox

import android.content.Context
import com.dropbox.core.android.Auth
import com.github.naz013.cloudapi.CloudKeysStorage
import com.github.naz013.logging.Logger

internal class DropboxAuthManagerImpl(
  private val context: Context,
  private val cloudKeysStorage: CloudKeysStorage
) : DropboxAuthManager {

  override fun isAuthorized(): Boolean {
    return (cloudKeysStorage.getDropboxToken().isNotEmpty()).also {
      Logger.i(TAG, "Dropbox is authorized: $it")
    }
  }

  override fun getOAuth2Token(): String {
    return cloudKeysStorage.getDropboxToken()
  }

  override fun removeOAuth2Token() {
    cloudKeysStorage.saveDropboxToken("")
    Logger.i(TAG, "Dropbox token removed")
  }

  override fun startAuth() {
    Logger.i(TAG, "Starting Dropbox auth")
    Auth.startOAuth2Authentication(context, APP_KEY)
  }

  override fun onAuthFinished() {
    Auth.getOAuth2Token()?.also {
      cloudKeysStorage.saveDropboxToken(it)
      Logger.i(TAG, "Dropbox token saved")
    }
  }

  companion object {
    private const val TAG = "DropboxAuthManager"
    private const val APP_KEY = "4zi1d414h0v8sxe"
  }
}
