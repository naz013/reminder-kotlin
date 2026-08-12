package com.github.naz013.cloudapi.googletasks

import android.content.Context
import com.github.naz013.cloudapi.CloudKeysStorage
import com.github.naz013.logging.Logger
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.services.tasks.TasksScopes

internal class GoogleTasksAuthManagerImpl(
  private val context: Context,
  private val cloudKeysStorage: CloudKeysStorage
) : GoogleTasksAuthManager {

  override fun isAuthorized(): Boolean {
    val userName = cloudKeysStorage.getGoogleTasksUserName()
    return (userName.isNotEmpty() && userName.matches(".*@.*".toRegex())).also {
      Logger.i(TAG, "Google Tasks is authorized: $it")
    }
  }

  override fun hasGooglePlayServices(): Boolean {
    val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
    return (resultCode == ConnectionResult.SUCCESS).also {
      Logger.d(TAG, "Google Play Services available: $it")
    }
  }

  override fun getUserName(): String {
    return cloudKeysStorage.getGoogleTasksUserName()
  }

  override fun removeUserName() {
    cloudKeysStorage.saveGoogleTasksUserName("")
    Logger.d(TAG, "Google Tasks user name removed")
  }

  override fun saveUserName(name: String) {
    cloudKeysStorage.saveGoogleTasksUserName(name)
    Logger.d(TAG, "Google Tasks user name saved")
  }

  override fun getScopes(): List<String> {
    return listOf(
      TasksScopes.TASKS
    )
  }

  companion object {
    private const val TAG = "GoogleTasksAuthManager"
  }
}
