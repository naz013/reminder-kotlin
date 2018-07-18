package com.elementary.tasks.login

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask

import com.elementary.tasks.R
import com.elementary.tasks.core.appWidgets.UpdatesHelper
import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.ContextHolder
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.groups.GroupsUtil

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
class RestoreGoogleTask(context: Context, private val mListener: (() -> Unit)?) : AsyncTask<Void, String, Void>() {
    private val mContext: ContextHolder = ContextHolder(context)
    private var mDialog: ProgressDialog? = null

    override fun onPreExecute() {
        super.onPreExecute()
        try {
            mDialog = ProgressDialog(mContext.context)
            mDialog!!.setTitle(mContext.context.getString(R.string.sync))
            mDialog!!.setCancelable(false)
            mDialog!!.setMessage(mContext.context.getString(R.string.please_wait))
            mDialog!!.show()
        } catch (e: Exception) {
            mDialog = null
        }

    }

    override fun onProgressUpdate(vararg values: String) {
        super.onProgressUpdate(*values)
        if (mDialog != null) {
            mDialog!!.setMessage(values[0])
            mDialog!!.show()
        }
    }

    override fun doInBackground(vararg params: Void): Void? {
        val drive = Google.getInstance()
        if (drive?.drive != null) {
            publishProgress(mContext.context.getString(R.string.syncing_groups))
            try {
                drive.drive!!.downloadGroups(false)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }

            val list = AppDb.getAppDatabase(mContext.context).groupDao().all
            if (list.isEmpty()) {
                val defUiID = GroupsUtil.initDefault(mContext.context)
                val items = AppDb.getAppDatabase(mContext.context).reminderDao().all
                val dao = AppDb.getAppDatabase(mContext.context).reminderDao()
                for (item in items) {
                    item.groupUuId = defUiID
                    dao.insert(item)
                }
            }

            publishProgress(mContext.context.getString(R.string.syncing_reminders))
            try {
                drive.drive!!.downloadReminders(mContext.context, false)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }

            //export & import notes
            publishProgress(mContext.context.getString(R.string.syncing_notes))
            try {
                drive.drive!!.downloadNotes(false)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }

            //export & import birthdays
            publishProgress(mContext.context.getString(R.string.syncing_birthdays))
            try {
                drive.drive!!.downloadBirthdays(false)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }

            //export & import places
            publishProgress(mContext.context.getString(R.string.syncing_places))
            try {
                drive.drive!!.downloadPlaces(false)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }

            //export & import templates
            publishProgress(mContext.context.getString(R.string.syncing_templates))
            try {
                drive.drive!!.downloadTemplates(false)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }

            try {
                drive.drive!!.downloadSettings(mContext.context, false)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }

        }
        return null
    }

    override fun onPostExecute(aVoid: Void) {
        super.onPostExecute(aVoid)
        if (mDialog != null && mDialog!!.isShowing) {
            try {
                mDialog!!.dismiss()
            } catch (e: IllegalArgumentException) {
                LogUtil.d(TAG, "onPostExecute: " + e.localizedMessage)
            }

        }
        UpdatesHelper.getInstance(mContext.context).updateWidget()
        UpdatesHelper.getInstance(mContext.context).updateNotesWidget()
        mListener?.invoke()
    }
    companion object {

        private const val TAG = "RestoreGoogleTask"
    }
}
