package com.elementary.tasks.navigation.settings.export

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.appWidgets.UpdatesHelper
import com.elementary.tasks.core.cloud.DropboxLogin
import com.elementary.tasks.core.cloud.GDrive
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.cloud.GoogleLogin
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.navigation.settings.BaseSettingsFragment
import com.google.api.services.tasks.model.TaskLists
import kotlinx.android.synthetic.main.fragment_settings_cloud_drives.*
import kotlinx.android.synthetic.main.view_progress.*
import timber.log.Timber
import java.io.IOException
import java.util.*
import javax.inject.Inject

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

    @Inject
    lateinit var updatesHelper: UpdatesHelper
    private val mDropboxCallback = object : DropboxLogin.LoginCallback {
        override fun onSuccess(b: Boolean) {
            if (b) {
                linkDropbox.text = getString(R.string.disconnect)
            } else {
                linkDropbox.text = getString(R.string.connect)
            }
            callback?.refreshMenu()
        }
    }

    init {
        ReminderApp.appComponent.inject(this)
    }

    private fun showErrorDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setMessage(getString(R.string.failed_to_login))
        builder.setPositiveButton(R.string.ok) { dialogInterface, _ -> dialogInterface.dismiss() }
        builder.create().show()
    }

    override fun layoutRes(): Int = R.layout.fragment_settings_cloud_drives

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateProgress(false)
        mDropbox = DropboxLogin(activity!!, mDropboxCallback)
        mGoogleLogin = GoogleLogin(activity!!, prefs)
        initDropboxButton()
        initGoogleDriveButton()
        initGoogleTasksButton()

        checkGoogleStatus()
    }

    private fun initGoogleTasksButton() {
        linkGTasks.setOnClickListener { googleTasksButtonClick() }
    }

    private fun initGoogleDriveButton() {
        linkGDrive.setOnClickListener { googleDriveButtonClick() }
    }

    private fun googleTasksButtonClick() {
        if (Permissions.checkPermission(activity!!,
                        Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
                        Permissions.WRITE_EXTERNAL)) {
            switchGoogleTasksStatus()
        } else {
            Permissions.requestPermission(activity!!, 104,
                    Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
                    Permissions.WRITE_EXTERNAL)
        }
    }

    private fun googleDriveButtonClick() {
        if (Permissions.checkPermission(activity!!,
                        Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
                        Permissions.WRITE_EXTERNAL)) {
            switchGoogleDriveStatus()
        } else {
            Permissions.requestPermission(activity!!, 103,
                    Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
                    Permissions.WRITE_EXTERNAL)
        }
    }

    private fun initDropboxButton() {
        linkDropbox.setOnClickListener { mDropbox.login() }
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
                        loadGoogleTasks()
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

    private fun loadGoogleTasks() {
        val gTasks = GTasks.getInstance(context!!) ?: return
        checkGoogleStatus()
        updateProgress(true)
        launchDefault {
            val appDb: AppDb = AppDb.getAppDatabase(context!!)

            var lists: TaskLists? = null
            try {
                lists = gTasks.taskLists()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            if (lists != null && lists.size > 0 && lists.items != null) {
                for (item in lists.items) {
                    val listId = item.id
                    var taskList = appDb.googleTaskListsDao().getById(listId)
                    if (taskList != null) {
                        taskList.update(item)
                    } else {
                        val r = Random()
                        val color = r.nextInt(15)
                        taskList = GoogleTaskList(item, color)
                    }
                    Timber.d("loadGoogleTasks: $taskList")
                    appDb.googleTaskListsDao().insert(taskList)
                    val tasks = gTasks.getTasks(listId)
                    if (tasks.isNotEmpty()) {
                        for (task in tasks) {
                            var googleTask = appDb.googleTasksDao().getById(task.id)
                            if (googleTask != null) {
                                googleTask.update(task)
                                googleTask.listId = task.id
                            } else {
                                googleTask = GoogleTask(task, listId)
                            }
                            appDb.googleTasksDao().insert(googleTask)
                        }
                    }
                }
                val local = appDb.googleTaskListsDao().all()
                if (local.isNotEmpty()) {
                    val listItem = local[0].apply {
                        this.def = 1
                        this.systemDefault = 1
                    }
                    appDb.googleTaskListsDao().insert(listItem)
                }
            }

            withUIContext {
                updatesHelper.updateTasksWidget()
                updateProgress(false)
            }
        }
    }

    private fun updateProgress(loading: Boolean) {
        if (loading) {
            progressView.visibility = View.VISIBLE
        } else {
            progressView.visibility = View.GONE
        }
        linkDropbox.isEnabled = !loading
        linkGDrive.isEnabled = !loading
        linkGTasks.isEnabled = !loading
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
                updatesHelper.updateTasksWidget()
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
        if (grantResults.isEmpty()) return
        when (requestCode) {
            103 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                switchGoogleDriveStatus()
            }
            104 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                switchGoogleTasksStatus()
            }
        }
    }

    private fun checkGoogleStatus() {
        if (mGoogleLogin.isGoogleDriveLogged) {
            linkGDrive.text = getString(R.string.disconnect)
        } else {
            linkGDrive.text = getString(R.string.connect)
        }
        if (mGoogleLogin.isGoogleTasksLogged) {
            linkGTasks.text = getString(R.string.disconnect)
        } else {
            linkGTasks.text = getString(R.string.connect)
        }
    }

    override fun onBackStackResume() {
        super.onBackStackResume()
        mDropbox.checkDropboxStatus()
        checkGoogleStatus()
    }

    override fun getTitle(): String = getString(R.string.cloud_services)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mGoogleLogin.onActivityResult(requestCode, resultCode, data)
    }
}
