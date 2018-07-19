package com.elementary.tasks.navigation.settings.export

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.DropboxLogin
import com.elementary.tasks.core.cloud.GoogleLogin
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.google_tasks.work.GetTaskListAsync
import com.elementary.tasks.google_tasks.work.TasksCallback
import com.elementary.tasks.navigation.settings.BaseSettingsFragment
import com.mcxiaoke.koi.ext.onClick
import kotlinx.android.synthetic.main.fragment_cloud_drives.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch

/**
 * Copyright 2016 Nazar Suhovich
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
class FragmentCloudDrives : BaseSettingsFragment() {

    private lateinit var mDropbox: DropboxLogin
    private lateinit var mGoogleLogin: GoogleLogin

    private var mDialog: ProgressDialog? = null
    private val mLoginCallback = object : GoogleLogin.LoginCallback {
        override fun onSuccess() {
            startSync()
        }

        override fun onFail() {
            showErrorDialog()
        }
    }
    private val mDropboxCallback = object : DropboxLogin.LoginCallback {
        override fun onSuccess(logged: Boolean) {
            if (logged) {
                linkDropbox.text = getString(R.string.disconnect)
            } else {
                linkDropbox.text = getString(R.string.connect)
            }
        }
    }

    private fun showErrorDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setMessage(getString(R.string.failed_to_login))
        builder.setPositiveButton(R.string.ok) { dialogInterface, i -> dialogInterface.dismiss() }
        builder.create().show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_cloud_drives, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mDropbox = DropboxLogin(activity!!, mDropboxCallback)
        mGoogleLogin = GoogleLogin(activity!!, mLoginCallback)
        initDropboxButton()
        initGoogleDriveButton()
        checkGoogleStatus()
    }

    private fun initGoogleDriveButton() {
        linkGDrive.onClick { googleDriveButtonClick() }
    }

    private fun googleDriveButtonClick() {
        if (Permissions.checkPermission(activity!!,
                        Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
                        Permissions.WRITE_EXTERNAL)) {
            switchGoogleStatus()
        } else {
            Permissions.requestPermission(activity!!, 103,
                    Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
                    Permissions.WRITE_EXTERNAL)
        }
    }

    private fun initDropboxButton() {
        linkDropbox.onClick { mDropbox.login() }
    }

    private fun switchGoogleStatus() {
        if (!SuperUtil.checkGooglePlayServicesAvailability(activity!!)) {
            Toast.makeText(context, R.string.google_play_services_not_installed, Toast.LENGTH_SHORT).show()
            return
        }
        if (mGoogleLogin.isLogged) {
            disconnectFromGoogleServices()
        } else {
            mGoogleLogin.login()
        }
    }

    private fun disconnectFromGoogleServices() {
        mGoogleLogin.logOut()
        launch(CommonPool) {
            AppDb.getAppDatabase(context!!).googleTasksDao().deleteAll()
            AppDb.getAppDatabase(context!!).googleTaskListsDao().deleteAll()
            withUIContext { finishSync() }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.isEmpty()) return
        when (requestCode) {
            103 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                switchGoogleStatus()
            }
        }
    }

    private fun checkGoogleStatus() {
        if (mGoogleLogin.isLogged) {
            linkGDrive.setText(R.string.disconnect)
        } else {
            linkGDrive.text = getString(R.string.connect)
        }
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback?.onTitleChange(getString(R.string.cloud_services))
            callback?.onFragmentSelect(this)
        }
        mDropbox.checkDropboxStatus()
        checkGoogleStatus()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mGoogleLogin.onActivityResult(requestCode, resultCode, data)
    }

    private fun startSync() {
        mDialog = ProgressDialog.show(context, null, getString(R.string.retrieving_tasks), false, true)
        GetTaskListAsync(context!!, object : TasksCallback {
            override fun onFailed() {
                finishSync()
            }

            override fun onComplete() {
                finishSync()
            }
        }).execute()
    }

    private fun finishSync() {
        if (mDialog != null && mDialog!!.isShowing) mDialog!!.dismiss()
        checkGoogleStatus()
        if (callback != null) callback!!.refreshMenu()
    }
}
