package com.elementary.tasks.core.cloud

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.Module
import androidx.appcompat.app.AlertDialog

/**
 * Copyright 2018 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class DropboxLogin(private val mContext: Activity, private val mCallback: DropboxLogin.LoginCallback) {
    private val mDropbox: Dropbox

    init {
        this.mDropbox = Dropbox(mContext)
        this.mDropbox.startSession()
    }

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
        if (mDropbox.isLinked) {
            if (mDropbox.unlink()) {
                mCallback.onSuccess(false)
            }
        } else {
            mDropbox.startLink()
        }
    }

    fun checkDropboxStatus() {
        LogUtil.d(TAG, "checkDropboxStatus: " + mDropbox.isLinked)
        if (mDropbox.isLinked) {
            mCallback.onSuccess(true)
        } else {
            LogUtil.d(TAG, "checkDropboxStatus2: " + mDropbox.isLinked)
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
        var installed: Boolean
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            installed = true
        } catch (e: PackageManager.NameNotFoundException) {
            installed = false
        }

        return installed
    }

    private fun checkDialog(): Dialog {
        return AlertDialog.Builder(mContext)
                .setMessage(mContext.getString(R.string.other_version_detected))
                .setPositiveButton(mContext.getString(R.string.open)) { dialogInterface, i -> openApp() }
                .setNegativeButton(mContext.getString(R.string.delete)) { dialogInterface, i -> deleteApp() }
                .setNeutralButton(mContext.getString(R.string.cancel)) { dialogInterface, i -> dialogInterface.dismiss() }
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
        if (Module.isPro) {
            i = manager.getLaunchIntentForPackage(MARKET_APP_JUSTREMINDER)
        } else {
            i = manager.getLaunchIntentForPackage(MARKET_APP_JUSTREMINDER_PRO)
        }
        i?.addCategory(Intent.CATEGORY_LAUNCHER)
        mContext.startActivity(i)
    }

    interface LoginCallback {
        fun onSuccess(b: Boolean)
    }

    companion object {

        val TAG = "DropboxLogin"
        val MARKET_APP_JUSTREMINDER = "com.cray.software.justreminder"
        val MARKET_APP_JUSTREMINDER_PRO = "com.cray.software.justreminderpro"
    }
}