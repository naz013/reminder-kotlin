package com.elementary.tasks.login

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.RealmDb
import java.io.IOException

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

class RestoreLocalTask(context: Context, listener: SyncListener) : AsyncTask<Void, String, Int>() {

    private var mContext: Context = context
    private var mListener: SyncListener = listener
    private var mDialog: ProgressDialog = ProgressDialog(context)

    override fun onPreExecute() {
        super.onPreExecute()
        mDialog.setTitle(mContext.getString(R.string.sync))
        mDialog.setMessage(mContext.getString(R.string.please_wait))
        mDialog.show()
    }

    override fun onProgressUpdate(vararg values: String) {
        super.onProgressUpdate(*values)
        mDialog.setMessage(values[0])
        mDialog.setCancelable(false)
        mDialog.show()
    }

    override fun doInBackground(vararg p0: Void?): Int {
        publishProgress(mContext.getString(R.string.syncing_groups))
        try {
            BackupTool.getInstance().importGroups()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val list = RealmDb.getInstance().allGroups
        if (list.size == 0) {
            val defUiID = RealmDb.getInstance().setDefaultGroups(mContext)
            val items = RealmDb.getInstance().allReminders
            for (item in items) {
                item.groupUuId = defUiID
                RealmDb.getInstance().saveObject(item)
            }
        }

        publishProgress(mContext.getString(R.string.syncing_reminders))
        try {
            BackupTool.getInstance().importReminders()
        } catch (e: IOException) {
            e.printStackTrace()
        }


        //export & import notes
        publishProgress(mContext.getString(R.string.syncing_notes))
        try {
            BackupTool.getInstance().importNotes()
        } catch (e: IOException) {
            e.printStackTrace()
        }


        //export & import birthdays
        publishProgress(mContext.getString(R.string.syncing_birthdays))
        try {
            BackupTool.getInstance().importBirthdays()
        } catch (e: IOException) {
            e.printStackTrace()
        }


        //export & import places
        publishProgress(mContext.getString(R.string.syncing_places))
        try {
            BackupTool.getInstance().importPlaces()
        } catch (e: IOException) {
            e.printStackTrace()
        }


        //export & import templates
        publishProgress(mContext.getString(R.string.syncing_templates))
        try {
            BackupTool.getInstance().importTemplates()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            Prefs.getInstance(mContext).loadPrefsFromFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return 1
    }

    override fun onPostExecute(aVoid: Int) {
        super.onPostExecute(aVoid)
        if (mDialog.isShowing) {
            mDialog.dismiss()
        }
        UpdatesHelper.getInstance(mContext).updateWidget()
        UpdatesHelper.getInstance(mContext).updateNotesWidget()
        mListener.onFinish()
    }

    interface SyncListener {
        fun onFinish() {

        }
    }
}