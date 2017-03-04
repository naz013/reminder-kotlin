package com.elementary.tasks.core.cloud

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Module

/**
 * Copyright 2017 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class DropboxLogin(context: Activity, callback: LoginCallback) {

    companion object {
        const val MARKET_APP_JUSTREMINDER: String = "com.cray.software.justreminder"
        const val MARKET_APP_JUSTREMINDER_PRO: String = "com.cray.software.justreminderpro"
    }

    private var mContext: Activity = context
    private var mDropbox: Dropbox = Dropbox(context)
    private var mCallback: LoginCallback = callback

    init {
        mDropbox.startSession()
    }

    fun login() {
        var isIn = isAppInstalled(MARKET_APP_JUSTREMINDER_PRO)
        if (Module.isPro()) isIn = isAppInstalled(MARKET_APP_JUSTREMINDER)
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
        if (mDropbox.checkLink() && mDropbox.isLinked) {
            mCallback.onSuccess(true)
        } else if (mDropbox.isLinked) {
            mCallback.onSuccess(true)
        } else {
            mCallback.onSuccess(false)
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
                .setPositiveButton(mContext.getString(R.string.open), { _, _ ->
                    val i: Intent
                    val manager = mContext.packageManager
                    if (Module.isPro()) {
                        i = manager.getLaunchIntentForPackage(MARKET_APP_JUSTREMINDER)
                    } else {
                        i = manager.getLaunchIntentForPackage(MARKET_APP_JUSTREMINDER_PRO)
                    }
                    i.addCategory(Intent.CATEGORY_LAUNCHER)
                    mContext.startActivity(i)
                })
                .setNegativeButton(mContext.getString(R.string.delete), { _, _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    if (Module.isPro()) {
                        intent.data = Uri.parse("package:" + MARKET_APP_JUSTREMINDER)
                    } else {
                        intent.data = Uri.parse("package:" + MARKET_APP_JUSTREMINDER_PRO)
                    }
                    mContext.startActivity(intent)
                })
                .setNeutralButton(mContext.getString(R.string.cancel), { dialog, _ -> dialog.dismiss() })
                .setCancelable(true)
                .create()
    }

    interface LoginCallback {
        fun onSuccess(logged: Boolean) {

        }
    }
}