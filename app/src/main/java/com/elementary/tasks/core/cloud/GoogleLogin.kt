package com.elementary.tasks.core.cloud

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
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
class GoogleLogin(private val activity: Activity, private val prefs: Prefs) {

    private var mGoogleTasks: GTasks? = GTasks.getInstance(activity)
    private var mGoogleDrive: GDrive? = GDrive.getInstance(activity)
    private var mAccountName: String? = null

    private var rtIntent: Intent? = null
    private var mDriveCallback: DriveCallback? = null
    private var mTasksCallback: TasksCallback? = null
    private var isDriveLogin = false

    var isGoogleDriveLogged = false
        private set
        get() {
            return mGoogleDrive?.isLogged ?: false
        }

    var isGoogleTasksLogged = false
        private set
        get() {
            return mGoogleTasks?.isLogged ?: false
        }

    fun logOutDrive() {
        mGoogleDrive?.logOut()
        mGoogleDrive = null
    }

    fun logOutTasks() {
        mGoogleTasks?.logOut()
        mGoogleTasks = null
    }

    fun loginDrive(loginCallback: DriveCallback) {
        isDriveLogin = true
        mDriveCallback = loginCallback
        askIntent()
    }

    private fun askIntent() {
        val intent = AccountPicker.newChooseAccountIntent(null, null,
                arrayOf("com.google"), false, null, null, null, null)
        activity.startActivityForResult(intent, REQUEST_AUTHORIZATION)
    }

    fun loginTasks(loginCallback: TasksCallback) {
        isDriveLogin = false
        mTasksCallback = loginCallback
        askIntent()
    }

    private fun getAndUseAuthTokenInAsyncTask(account: Account) {
        launchDefault {
            withUIContext { showProgress() }
            val token = getAccessToken(account)
            withUIContext {
                hideProgress()
                if (token != null) {
                    if (token == RT_CODE) {
                        if (rtIntent != null) {
                            activity.startActivityForResult(rtIntent, REQUEST_ACCOUNT_PICKER)
                        } else {
                            sendFail()
                        }
                    } else {
                        finishLogin(mAccountName!!)
                    }
                } else {
                    sendFail()
                }
            }
        }
    }

    private fun sendFail() {
        if (isDriveLogin) {
            mDriveCallback?.onFail()
        } else {
            mTasksCallback?.onFail()
        }
    }

    private fun hideProgress() {
        Timber.d("hideProgress: ")
        if (isDriveLogin) mDriveCallback?.onProgress(false)
        else mTasksCallback?.onProgress(false)
    }

    private fun showProgress() {
        Timber.d("showProgress: ")
        if (isDriveLogin) mDriveCallback?.onProgress(true)
        else mTasksCallback?.onProgress(true)
    }

    private fun getAccessToken(account: Account): String? {
        Timber.d("getAccessToken: ")
        try {
            val scope = "oauth2:" + if (isDriveLogin) {
                DriveScopes.DRIVE
            } else {
                TasksScopes.TASKS
            }
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

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_AUTHORIZATION && resultCode == RESULT_OK) {
            mAccountName = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            val gam = GoogleAccountManager(activity)
            getAndUseAuthTokenInAsyncTask(gam.getAccountByName(mAccountName))
        } else if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode == RESULT_OK) {
            mAccountName = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            val accountName = mAccountName
            if (accountName != null) {
                finishLogin(accountName)
            } else {
                sendFail()
            }
        } else {
            sendFail()
        }
    }

    private fun finishLogin(account: String) {
        if (isDriveLogin) {
            mGoogleDrive?.logOut()
            prefs.driveUser = account
            mGoogleDrive = GDrive.getInstance(activity)
            mDriveCallback?.onResult(mGoogleDrive, mGoogleDrive?.isLogged == true)
        } else {
            mGoogleTasks?.logOut()
            prefs.tasksUser = account
            mGoogleTasks = GTasks.getInstance(activity)
            mTasksCallback?.onResult(mGoogleTasks, mGoogleTasks?.isLogged == true)
        }
    }

    interface LoginCallback<V> {
        fun onProgress(isLoading: Boolean)

        fun onResult(v: V?, isLogged: Boolean)

        fun onFail()
    }

    interface DriveCallback : LoginCallback<GDrive>

    interface TasksCallback : LoginCallback<GTasks>

    companion object {

        private const val REQUEST_AUTHORIZATION = 1
        private const val REQUEST_ACCOUNT_PICKER = 3
        private const val RT_CODE = "rt"
    }
}
