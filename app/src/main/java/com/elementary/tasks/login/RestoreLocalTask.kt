package com.elementary.tasks.login

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask

import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.appWidgets.UpdatesHelper
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.ContextHolder
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.groups.GroupsUtil

import java.io.IOException
import javax.inject.Inject

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
class RestoreLocalTask internal constructor(context: Context, private val mListener: (() -> Unit)?) : AsyncTask<Void, String, Int>() {

    private val mContext: ContextHolder = ContextHolder(context)
    private var mDialog: ProgressDialog? = null

    @Inject
    lateinit var updatesHelper: UpdatesHelper
    @Inject
    lateinit var backupTool: BackupTool
    @Inject
    lateinit var prefs: Prefs

    init {
        ReminderApp.appComponent.inject(this)
        this.mDialog = ProgressDialog(context)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        try {
            val dialog = this.mDialog
            if (dialog != null) {
                dialog.setTitle(mContext.context.getString(R.string.sync))
                dialog.setMessage(mContext.context.getString(R.string.please_wait))
                dialog.show()
                this.mDialog = dialog
            }
        } catch (var2: Exception) {
            this.mDialog = null
        }

    }

    override fun onProgressUpdate(vararg values: String) {
        super.onProgressUpdate(*values)
        mDialog?.setMessage(values[0])
        mDialog?.setCancelable(false)
        mDialog?.show()
    }

    override fun doInBackground(vararg p0: Void): Int {
        publishProgress(mContext.context.getString(R.string.syncing_groups))
        try {
            backupTool.importGroups()
        } catch (ignored: IOException) {
        }

        val list = AppDb.getAppDatabase(mContext.context).reminderGroupDao().all()
        if (list.isEmpty()) {
            val defUiID = GroupsUtil.initDefault(mContext.context)
            val items = AppDb.getAppDatabase(mContext.context).reminderDao().all()
            val dao = AppDb.getAppDatabase(mContext.context).reminderDao()
            for (item in items) {
                item.groupUuId = defUiID
                dao.insert(item)
            }
        }

        this.publishProgress(mContext.context.getString(R.string.syncing_reminders))
        try {
            backupTool.importReminders(this.mContext.context)
        } catch (ignored: IOException) {
        }

        this.publishProgress(mContext.context.getString(R.string.syncing_notes))
        try {
            backupTool.importNotes()
        } catch (ignored: IOException) {
        }

        this.publishProgress(mContext.context.getString(R.string.syncing_birthdays))
        try {
            backupTool.importBirthdays()
        } catch (ignored: IOException) {
        }

        publishProgress(mContext.context.getString(R.string.syncing_places))
        try {
            backupTool.importPlaces()
        } catch (ignored: IOException) {
        }

        publishProgress(mContext.context.getString(R.string.syncing_templates))
        try {
            backupTool.importTemplates()
        } catch (ignored: IOException) {
        }

        prefs.loadPrefsFromFile()
        return 1
    }

    override fun onPostExecute(integer: Int?) {
        super.onPostExecute(integer)
        try {
            mDialog!!.dismiss()
        } catch (e: Exception) {
            LogUtil.d("RestoreLocalTask", "onPostExecute: " + e.localizedMessage)
        }

        updatesHelper.updateWidget()
        updatesHelper.updateNotesWidget()
        mListener?.invoke()
    }
}
