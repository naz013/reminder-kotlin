package com.elementary.tasks.core.cloud

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.storages.Dropbox
import com.elementary.tasks.core.utils.Module
import timber.log.Timber

class DropboxLogin(private val mContext: Activity, private val mCallback: DropboxLogin.LoginCallback) {

    private val mDropbox: Dropbox = Dropbox()

    init {
        this.mDropbox.startSession()
    }

    fun login() {
        var isIn = isAppInstalled(MARKET_APP_JUSTREMINDER_PRO)
        if (Module.isPro) isIn = isAppInstalled(MARKET_APP_JUSTREMINDER)
        if (isIn) {
            checkDialog().show()
        } else {
            performDropboxLinking(mContext)
        }
    }

    private fun performDropboxLinking(context: Context) {
        if (mDropbox.isLinked) {
            if (mDropbox.unlink()) {
                mCallback.onSuccess(false)
            }
        } else {
            mDropbox.startLink(context)
        }
    }

    fun checkDropboxStatus() {
        Timber.d("checkDropboxStatus: ${mDropbox.isLinked}")
        if (mDropbox.isLinked) {
            mCallback.onSuccess(true)
        } else {
            mDropbox.startSession()
            if (mDropbox.isLinked) {
                mCallback.onSuccess(true)
            } else {
                mCallback.onSuccess(false)
            }
        }
    }

    private fun isAppInstalled(packageName: String): Boolean {
        val pm = mContext.packageManager
        return try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun checkDialog(): Dialog {
        return AlertDialog.Builder(mContext)
                .setMessage(mContext.getString(R.string.other_version_detected))
                .setPositiveButton(mContext.getString(R.string.open)) { _, _ -> openApp() }
                .setNegativeButton(mContext.getString(R.string.delete)) { _, _ -> deleteApp() }
                .setNeutralButton(mContext.getString(R.string.cancel)) { dialogInterface, _ -> dialogInterface.dismiss() }
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
        mContext.startActivity(intent)
    }

    private fun openApp() {
        val i: Intent?
        val manager = mContext.packageManager
        i = if (Module.isPro) {
            manager.getLaunchIntentForPackage(MARKET_APP_JUSTREMINDER)
        } else {
            manager.getLaunchIntentForPackage(MARKET_APP_JUSTREMINDER_PRO)
        }
        i?.addCategory(Intent.CATEGORY_LAUNCHER)
        mContext.startActivity(i)
    }

    interface LoginCallback {
        fun onSuccess(b: Boolean)
    }

    companion object {
        const val MARKET_APP_JUSTREMINDER = "com.cray.software.justreminder"
        const val MARKET_APP_JUSTREMINDER_PRO = "com.cray.software.justreminderpro"
    }
}
