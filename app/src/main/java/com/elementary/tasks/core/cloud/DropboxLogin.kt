package com.elementary.tasks.core.cloud

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Module
import com.github.naz013.cloudapi.dropbox.DropboxApi
import com.github.naz013.cloudapi.dropbox.DropboxAuthManager

class DropboxLogin(
  private val activity: Activity,
  private val dropboxApi: DropboxApi,
  private val dropboxAuthManager: DropboxAuthManager,
  private val callback: LoginCallback
) {

  fun login() {
    var isIn = isAppInstalled(MARKET_APP_JUSTREMINDER_PRO)
    if (Module.isPro) isIn = isAppInstalled(MARKET_APP_JUSTREMINDER)
    if (isIn) {
      checkDialog().show()
    } else {
      performDropboxLinking()
    }
  }

  private fun performDropboxLinking() {
    if (dropboxAuthManager.isAuthorized()) {
      dropboxApi.disconnect()
      dropboxAuthManager.removeOAuth2Token()
      callback.onResult(false)
    } else {
      dropboxAuthManager.startAuth()
    }
  }

  fun checkAuthOnResume() {
    if (dropboxAuthManager.isAuthorized()) {
      callback.onResult(true)
    } else {
      dropboxAuthManager.onAuthFinished()
      dropboxApi.initialize()
      callback.onResult(dropboxAuthManager.isAuthorized())
    }
  }

  private fun isAppInstalled(packageName: String): Boolean {
    val pm = activity.packageManager
    return try {
      pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
      true
    } catch (e: PackageManager.NameNotFoundException) {
      false
    }
  }

  private fun checkDialog(): Dialog {
    return AlertDialog.Builder(activity)
      .setMessage(activity.getString(R.string.other_version_detected))
      .setPositiveButton(activity.getString(R.string.open)) { _, _ -> openApp() }
      .setNegativeButton(activity.getString(R.string.delete)) { _, _ -> deleteApp() }
      .setNeutralButton(activity.getString(R.string.cancel)) { dialogInterface, _ ->
        dialogInterface.dismiss()
      }
      .setCancelable(true)
      .create()
  }

  private fun deleteApp() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    if (Module.isPro) {
      intent.data = Uri.parse("package:$MARKET_APP_JUSTREMINDER")
    } else {
      intent.data = Uri.parse("package:$MARKET_APP_JUSTREMINDER_PRO")
    }
    activity.startActivity(intent)
  }

  private fun openApp() {
    val i: Intent?
    val manager = activity.packageManager
    i = if (Module.isPro) {
      manager.getLaunchIntentForPackage(MARKET_APP_JUSTREMINDER)
    } else {
      manager.getLaunchIntentForPackage(MARKET_APP_JUSTREMINDER_PRO)
    }
    i?.addCategory(Intent.CATEGORY_LAUNCHER)
    activity.startActivity(i)
  }

  interface LoginCallback {
    fun onResult(isSuccess: Boolean)
  }

  companion object {
    const val MARKET_APP_JUSTREMINDER = "com.cray.software.justreminder"
    const val MARKET_APP_JUSTREMINDER_PRO = "com.cray.software.justreminderpro"
  }
}
