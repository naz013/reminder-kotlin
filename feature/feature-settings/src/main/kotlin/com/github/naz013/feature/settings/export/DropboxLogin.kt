package com.github.naz013.feature.settings.export

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri
import com.github.naz013.cloudapi.dropbox.DropboxApi
import com.github.naz013.cloudapi.dropbox.DropboxAuthManager
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.platform.SystemInfo

class DropboxLogin(
  private val context: Context,
  private val dropboxApi: DropboxApi,
  private val dropboxAuthManager: DropboxAuthManager,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase,
  private val systemInfo: SystemInfo,
  private val buildInfo: BuildInfo
) {

  private var loginCallback: ((Boolean) -> Unit)? = null

  fun login(
    onAuthResult: (Boolean) -> Unit,
    onDuplicateFound: () -> Unit = {},
  ) {
    val hasOtherVariant = if (buildInfo.isPro) {
      systemInfo.isAppInstalled(SystemInfo.FREE_PACKAGE_NAME)
    } else {
      systemInfo.isAppInstalled(SystemInfo.PRO_PACKAGE_NAME)
    }
    if (hasOtherVariant) {
      onDuplicateFound()
    } else {
      loginCallback = onAuthResult
      performDropboxLinking()
    }
  }

  private fun performDropboxLinking() {
    if (dropboxAuthManager.isAuthorized()) {
      dropboxApi.disconnect()
      dropboxAuthManager.removeOAuth2Token()
      loginCallback?.invoke(false)
    } else {
      dropboxAuthManager.startAuth()
    }
  }

  fun checkAuthOnResume() {
    if (dropboxAuthManager.isAuthorized()) {
      loginCallback?.invoke(true)
    } else {
      dropboxAuthManager.onAuthFinished()
      dropboxApi.initialize()
      loginCallback?.invoke(dropboxAuthManager.isAuthorized())
      if (dropboxAuthManager.isAuthorized()) {
        scheduleBackgroundWorkUseCase(
          workType = WorkType.ForceSync,
          dataType = null,
          id = null,
          ids = null,
        )
      }
    }
  }

  fun deleteApp() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    if (buildInfo.isPro) {
      intent.data = "package:${SystemInfo.FREE_PACKAGE_NAME}".toUri()
    } else {
      intent.data = "package:${SystemInfo.PRO_PACKAGE_NAME}".toUri()
    }
    context.startActivity(intent)
  }

  fun openApp() {
    val intent = if (buildInfo.isPro) {
      context.packageManager.getLaunchIntentForPackage(SystemInfo.FREE_PACKAGE_NAME)
    } else {
      context.packageManager.getLaunchIntentForPackage(SystemInfo.PRO_PACKAGE_NAME)
    }
    intent?.addCategory(Intent.CATEGORY_LAUNCHER)
    intent?.also { context.startActivity(it) }
  }
}
