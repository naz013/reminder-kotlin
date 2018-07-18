package com.elementary.tasks.navigation.settings.export

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.elementary.tasks.core.views.roboto.RoboButton
import com.elementary.tasks.databinding.FragmentCloudDrivesBinding
import com.elementary.tasks.google_tasks.work.GetTaskListAsync
import com.elementary.tasks.google_tasks.work.TasksCallback
import com.elementary.tasks.navigation.settings.BaseSettingsFragment

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

    private var mDropbox: DropboxLogin? = null
    private var mGoogleLogin: GoogleLogin? = null

    private var binding: FragmentCloudDrivesBinding? = null
    private var mDropboxButton: RoboButton? = null
    private var mGoogleDriveButton: RoboButton? = null

    private var mDialog: ProgressDialog? = null
    private val mHandler = Handler(Looper.getMainLooper())
    private val mLoginCallback = object : GoogleLogin.LoginCallback {
        override fun onSuccess() {
            startSync()
        }

        override fun onFail() {
            showErrorDialog()
        }
    }
    private val mDropboxCallback = DropboxLogin.LoginCallback { logged ->
        if (logged) {
            mDropboxButton!!.text = getString(R.string.disconnect)
        } else {
            mDropboxButton!!.text = getString(R.string.connect)
        }
    }

    private fun showErrorDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setMessage(getString(R.string.failed_to_login))
        builder.setPositiveButton(R.string.ok) { dialogInterface, i -> dialogInterface.dismiss() }
        builder.create().show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCloudDrivesBinding.inflate(inflater, container, false)
        mDropbox = DropboxLogin(activity!!, mDropboxCallback)
        mGoogleLogin = GoogleLogin(activity, mLoginCallback)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initDropboxButton()
        initGoogleDriveButton()
        checkGoogleStatus()
    }

    private fun initGoogleDriveButton() {
        mGoogleDriveButton = binding!!.linkGDrive
        mGoogleDriveButton!!.setOnClickListener { v -> googleDriveButtonClick() }
    }

    private fun googleDriveButtonClick() {
        if (Permissions.checkPermission(activity,
                        Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
                        Permissions.WRITE_EXTERNAL)) {
            switchGoogleStatus()
        } else {
            Permissions.requestPermission(activity, 103,
                    Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
                    Permissions.WRITE_EXTERNAL)
        }
    }

    private fun initDropboxButton() {
        mDropboxButton = binding!!.linkDropbox
        mDropboxButton!!.setOnClickListener { v -> mDropbox!!.login() }
    }

    private fun switchGoogleStatus() {
        if (!SuperUtil.checkGooglePlayServicesAvailability(activity)) {
            Toast.makeText(context, R.string.google_play_services_not_installed, Toast.LENGTH_SHORT).show()
            return
        }
        if (mGoogleLogin!!.isLogged) {
            disconnectFromGoogleServices()
        } else {
            mGoogleLogin!!.login()
        }
    }

    private fun disconnectFromGoogleServices() {
        mGoogleLogin!!.logOut()
        Thread {
            AppDb.getAppDatabase(context).googleTasksDao().deleteAll()
            AppDb.getAppDatabase(context).googleTaskListsDao().deleteAll()
            mHandler.post { this.finishSync() }
        }.start()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.size == 0) return
        when (requestCode) {
            103 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                switchGoogleStatus()
            }
        }
    }

    private fun checkGoogleStatus() {
        if (mGoogleLogin!!.isLogged) {
            mGoogleDriveButton!!.setText(R.string.disconnect)
        } else {
            mGoogleDriveButton!!.text = getString(R.string.connect)
        }
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback!!.onTitleChange(getString(R.string.cloud_services))
            callback!!.onFragmentSelect(this)
        }
        mDropbox!!.checkDropboxStatus()
        checkGoogleStatus()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (mGoogleLogin != null) mGoogleLogin!!.onActivityResult(requestCode, resultCode, data)
    }

    private fun startSync() {
        mDialog = ProgressDialog.show(context, null, getString(R.string.retrieving_tasks), false, true)
        GetTaskListAsync(context, object : TasksCallback {
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
