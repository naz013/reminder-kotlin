package com.elementary.tasks.core.cloud

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Prefs
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.common.AccountPicker
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager
import com.google.api.services.drive.DriveScopes
import com.google.api.services.tasks.TasksScopes
import timber.log.Timber
import java.io.IOException

/**
 * Copyright 2017 Nazar Suhovich
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

class GoogleLogin(private val activity: Activity, private val mCallback: LoginCallback?) {

    private var mGoogle: Google? = Google.getInstance()
    private var mAccountName: String? = null
    private val mUiHandler = Handler(Looper.getMainLooper())
    private var mProgress: ProgressDialog? = null
    private var rtIntent: Intent? = null

    val isLogged: Boolean
        get() = mGoogle != null

    fun logOut() {
        Prefs.getInstance(activity).driveUser = Prefs.DRIVE_USER_NONE
        mGoogle!!.logOut()
        mGoogle = null
    }

    fun login() {
        val intent = AccountPicker.newChooseAccountIntent(null, null,
                arrayOf("com.google"), false, null, null, null, null)
        activity.startActivityForResult(intent, REQUEST_AUTHORIZATION)
    }

    private fun getAndUseAuthTokenInAsyncTask(account: Account) {
        mUiHandler.post { this.showProgress() }
        Thread {
            val token = getAccessToken(account)
            mUiHandler.post {
                hideProgress()
                if (token != null) {
                    if (token == RT_CODE) {
                        if (rtIntent != null) {
                            activity.startActivityForResult(rtIntent, REQUEST_ACCOUNT_PICKER)
                        } else {
                            mCallback?.onFail()
                        }
                    } else {
                        finishLogin(mAccountName!!)
                        mCallback?.onSuccess()
                    }
                } else {
                    mCallback?.onFail()
                }
            }
        }.start()
    }

    private fun hideProgress() {
        Timber.d("hideProgress: ")
        try {
            if (mProgress != null && mProgress!!.isShowing) {
                mProgress!!.dismiss()
            }
        } catch (ignored: IllegalArgumentException) {
        }
        mProgress = null
    }

    private fun showProgress() {
        Timber.d("showProgress: ")
        if (mProgress != null && mProgress!!.isShowing) return
        mProgress = ProgressDialog(activity, ProgressDialog.STYLE_SPINNER)
        mProgress!!.setMessage(activity.getString(R.string.trying_to_log_in))
        mProgress!!.setCancelable(false)
        mProgress!!.isIndeterminate = true
        mProgress!!.show()
    }

    private fun getAccessToken(account: Account): String? {
        Timber.d("getAccessToken: ")
        try {
            val scope = "oauth2:" + DriveScopes.DRIVE + " " + TasksScopes.TASKS
            val token = GoogleAuthUtil.getToken(activity, account, scope)
            Timber.d("getAccessToken: ok")
            return token
        } catch (e: UserRecoverableAuthException) {
            rtIntent = e.intent
            Timber.d("getAccessToken: re-try")
            return RT_CODE
        } catch (e: ActivityNotFoundException) {
            Timber.d("getAccessToken: null")
            return null
        } catch (e: GoogleAuthException) {
            Timber.d("getAccessToken: null")
            return null
        } catch (e: IOException) {
            Timber.d("getAccessToken: null")
            return null
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == REQUEST_AUTHORIZATION && resultCode == RESULT_OK) {
            mAccountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            val gam = GoogleAccountManager(activity)
            getAndUseAuthTokenInAsyncTask(gam.getAccountByName(mAccountName))
        } else if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode == RESULT_OK) {
            mAccountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (mAccountName != null) {
                finishLogin(mAccountName!!)
                mCallback?.onSuccess()
            } else {
                mCallback?.onFail()
            }
        } else {
            mCallback?.onFail()
        }
    }

    private fun finishLogin(account: String) {
        Prefs.getInstance(activity).driveUser = account
        mGoogle = Google.getInstance()
    }

    interface LoginCallback {
        fun onSuccess()

        fun onFail()
    }

    companion object {

        private const val REQUEST_AUTHORIZATION = 1
        private const val REQUEST_ACCOUNT_PICKER = 3
        private const val RT_CODE = "rt"
    }
}
