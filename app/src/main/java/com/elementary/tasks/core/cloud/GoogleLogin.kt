package com.elementary.tasks.core.cloud

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.launchIo
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.google.api.services.tasks.TasksScopes
import timber.log.Timber

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

    private var mDriveCallback: DriveCallback? = null
    private var mTasksCallback: TasksCallback? = null
    private var isDriveLogin = false

    var googleStatus: ((Boolean) -> Unit)? = null

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

        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
                .requestEmail()
                .build()
        val client = GoogleSignIn.getClient(activity, signInOptions)
        client.signOut().addOnSuccessListener {
            googleStatus?.invoke(false)
        }
    }

    fun logOutTasks() {
        mGoogleTasks?.logOut()
        mGoogleTasks = null

        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Scope(TasksScopes.TASKS))
                .requestEmail()
                .build()
        val client = GoogleSignIn.getClient(activity, signInOptions)
        client.signOut().addOnSuccessListener {
            googleStatus?.invoke(false)
        }
    }

    fun loginDrive(loginCallback: DriveCallback) {
        isDriveLogin = true
        mDriveCallback = loginCallback

        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
                .requestEmail()
                .build()
        val client = GoogleSignIn.getClient(activity, signInOptions)
        activity.startActivityForResult(client.signInIntent, REQUEST_CODE_SIGN_IN)
    }

    fun loginTasks(loginCallback: TasksCallback) {
        isDriveLogin = false
        mTasksCallback = loginCallback

        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Scope(TasksScopes.TASKS))
                .requestEmail()
                .build()
        val client = GoogleSignIn.getClient(activity, signInOptions)
        activity.startActivityForResult(client.signInIntent, REQUEST_CODE_SIGN_IN)
    }

    private fun sendFail() {
        if (isDriveLogin) {
            mDriveCallback?.onFail()
        } else {
            mTasksCallback?.onFail()
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_SIGN_IN && resultCode == RESULT_OK) {
            if (data != null) handleSignInResult(data)
            else sendFail()
        } else {
            sendFail()
        }
    }

    private fun handleSignInResult(result: Intent) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener { googleAccount ->
                    Timber.d("Signed in as ${googleAccount.email}")
                    finishLogin(googleAccount.account?.name ?: "")
                    testSync()
                }
                .addOnFailureListener {
                    Timber.d("handleSignInResult: ${it.message}")
                    sendFail()
                }
    }

    private fun testSync() {
        launchIo {
            GDrive.getInstance(activity)?.saveRemindersToDrive()
        }
    }

    private fun finishLogin(account: String) {
        Timber.d("finishLogin: $account")
        if (account.isEmpty()) {
            sendFail()
            return
        }
        if (isDriveLogin) {
            mGoogleDrive?.logOut()
            prefs.driveUser = account
            mGoogleDrive = GDrive.getInstance(activity)
            mGoogleDrive?.statusObserver = {
                googleStatus?.invoke(it)
            }
            mDriveCallback?.onResult(mGoogleDrive, mGoogleDrive?.isLogged == true)
        } else {
            mGoogleTasks?.logOut()
            prefs.tasksUser = account
            mGoogleTasks = GTasks.getInstance(activity)
            mGoogleTasks?.statusObserver = {
                googleStatus?.invoke(it)
            }
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
        private const val REQUEST_CODE_SIGN_IN = 4
    }
}
