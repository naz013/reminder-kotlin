package com.elementary.tasks.navigation.settings.export

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.cloud.DropboxLogin
import com.elementary.tasks.core.cloud.GDrive
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.cloud.GoogleLogin
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.databinding.FragmentSettingsCloudDrivesBinding
import com.elementary.tasks.navigation.settings.BaseSettingsFragment
import timber.log.Timber

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
class FragmentCloudDrives : BaseSettingsFragment<FragmentSettingsCloudDrivesBinding>() {

    private lateinit var viewModel: CloudViewModel
    private lateinit var mDropbox: DropboxLogin
    private lateinit var mGoogleLogin: GoogleLogin

    private val mDropboxCallback = object : DropboxLogin.LoginCallback {
        override fun onSuccess(b: Boolean) {
            if (b) {
                binding.linkDropbox.text = getString(R.string.disconnect)
            } else {
                binding.linkDropbox.text = getString(R.string.connect)
            }
            callback?.refreshMenu()
        }
    }

    private fun showErrorDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setMessage(getString(R.string.failed_to_login))
        builder.setPositiveButton(R.string.ok) { dialogInterface, _ -> dialogInterface.dismiss() }
        builder.create().show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(CloudViewModel::class.java)
    }

    override fun layoutRes(): Int = R.layout.fragment_settings_cloud_drives

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateProgress(false)
        binding.progressMessageView.text = getString(R.string.please_wait)
        mDropbox = DropboxLogin(activity!!, mDropboxCallback)
        mGoogleLogin = GoogleLogin(activity!!, prefs)
        mGoogleLogin.googleStatus = {
            checkGoogleStatus()
        }
        initDropboxButton()
        initGoogleDriveButton()
        initGoogleTasksButton()

        checkGoogleStatus()
    }

    override fun onStart() {
        super.onStart()

        viewModel.isLoading.observe(this, Observer {
            if (it != null) {
                updateProgress(it)
            }
        })
        viewModel.isReady.observe(this, Observer {
            if (it != null && it) {
                UpdatesHelper.updateTasksWidget(context!!)
            }
        })
    }

    private fun initGoogleTasksButton() {
        if (SuperUtil.isGooglePlayServicesAvailable(context!!)) {
            binding.tasksView.visibility = View.VISIBLE
            binding.linkGTasks.setOnClickListener { googleTasksButtonClick() }
        } else {
            binding.tasksView.visibility = View.GONE
        }
    }

    private fun initGoogleDriveButton() {
        if (SuperUtil.isGooglePlayServicesAvailable(context!!)) {
            binding.driveView.visibility = View.VISIBLE
            binding.linkGDrive.setOnClickListener { googleDriveButtonClick() }
        } else {
            binding.driveView.visibility = View.GONE
        }
    }

    private fun googleTasksButtonClick() {
        if (Permissions.ensurePermissions(activity!!, 104,
                        Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
                        Permissions.WRITE_EXTERNAL)) {
            switchGoogleTasksStatus()
        }
    }

    private fun googleDriveButtonClick() {
        if (Permissions.ensurePermissions(activity!!, 103,
                        Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
                        Permissions.WRITE_EXTERNAL)) {
            switchGoogleDriveStatus()
        }
    }

    private fun initDropboxButton() {
        binding.linkDropbox.setOnClickListener { mDropbox.login() }
    }

    private fun switchGoogleTasksStatus() {
        if (!SuperUtil.checkGooglePlayServicesAvailability(activity!!)) {
            Toast.makeText(context, R.string.google_play_services_not_installed, Toast.LENGTH_SHORT).show()
            return
        }
        if (mGoogleLogin.isGoogleTasksLogged) {
            disconnectFromGoogleTasks()
        } else {
            mGoogleLogin.loginTasks(object : GoogleLogin.TasksCallback {
                override fun onProgress(isLoading: Boolean) {
                    updateProgress(isLoading)
                }

                override fun onResult(v: GTasks?, isLogged: Boolean) {
                    Timber.d("onResult: $isLogged")
                    if (isLogged) {
                        viewModel.loadGoogleTasks()
                    }
                    checkGoogleStatus()
                    callback?.refreshMenu()
                }

                override fun onFail() {
                    showErrorDialog()
                    callback?.refreshMenu()
                }
            })
        }
    }

    private fun updateProgress(loading: Boolean) {
        if (loading) {
            binding.progressView.visibility = View.VISIBLE
        } else {
            binding.progressView.visibility = View.GONE
        }
        binding.linkDropbox.isEnabled = !loading
        binding.linkGDrive.isEnabled = !loading
        binding.linkGTasks.isEnabled = !loading
    }

    private fun switchGoogleDriveStatus() {
        if (!SuperUtil.checkGooglePlayServicesAvailability(activity!!)) {
            Toast.makeText(context, R.string.google_play_services_not_installed, Toast.LENGTH_SHORT).show()
            return
        }
        if (mGoogleLogin.isGoogleDriveLogged) {
            disconnectFromGoogleDrive()
        } else {
            mGoogleLogin.loginDrive(object : GoogleLogin.DriveCallback {
                override fun onProgress(isLoading: Boolean) {
                    updateProgress(isLoading)
                }

                override fun onResult(v: GDrive?, isLogged: Boolean) {
                    if (isLogged) {
                        checkGoogleStatus()
                    }
                    callback?.refreshMenu()
                }

                override fun onFail() {
                    showErrorDialog()
                    callback?.refreshMenu()
                }
            })
        }
    }

    private fun disconnectFromGoogleTasks() {
        mGoogleLogin.logOutTasks()
        updateProgress(true)
        launchDefault {
            AppDb.getAppDatabase(context!!).googleTasksDao().deleteAll()
            AppDb.getAppDatabase(context!!).googleTaskListsDao().deleteAll()
            withUIContext {
                UpdatesHelper.updateTasksWidget(context!!)
                callback?.refreshMenu()
                updateProgress(false)
                checkGoogleStatus()
            }
        }
    }

    private fun disconnectFromGoogleDrive() {
        mGoogleLogin.logOutDrive()
        checkGoogleStatus()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Permissions.isAllGranted(grantResults)) {
            when (requestCode) {
                103 -> switchGoogleDriveStatus()
                104 -> switchGoogleTasksStatus()
            }
        }
    }

    private fun checkGoogleStatus() {
        if (mGoogleLogin.isGoogleDriveLogged) {
            binding.linkGDrive.text = getString(R.string.disconnect)
        } else {
            binding.linkGDrive.text = getString(R.string.connect)
        }
        if (mGoogleLogin.isGoogleTasksLogged) {
            binding.linkGTasks.text = getString(R.string.disconnect)
        } else {
            binding.linkGTasks.text = getString(R.string.connect)
        }
    }

    override fun onBackStackResume() {
        super.onBackStackResume()
        mDropbox.checkDropboxStatus()
        checkGoogleStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        mGoogleLogin.googleStatus = null
    }

    override fun getTitle(): String = getString(R.string.cloud_services)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mGoogleLogin.onActivityResult(requestCode, resultCode, data)
    }
}
