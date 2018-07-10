package com.elementary.tasks.core.async

import android.content.Context
import android.os.AsyncTask

import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Group
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.ContextHolder
import com.elementary.tasks.core.utils.IoHelper
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.groups.GroupsUtil

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

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

class SyncTask(context: Context, private val mListener: SyncListener?, private val quiet: Boolean) : AsyncTask<Void, String, Boolean>() {

    private val mContext: ContextHolder?
    private var mNotifyMgr: NotificationManagerCompat? = null
    private val builder: NotificationCompat.Builder

    init {
        this.mContext = ContextHolder(context)
        builder = NotificationCompat.Builder(context, Notifier.CHANNEL_SYSTEM)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        if (!quiet) {
            builder.setContentTitle(if (Module.isPro)
                mContext!!.context.getString(R.string.app_name_pro)
            else
                mContext!!.context.getString(R.string.app_name))
            builder.setContentText(mContext.context.getString(R.string.sync))
            if (Module.isLollipop) {
                builder.setSmallIcon(R.drawable.ic_cached_white_24dp)
            } else {
                builder.setSmallIcon(R.drawable.ic_cached_nv_white)
            }
            mNotifyMgr = NotificationManagerCompat.from(mContext.context)
            mNotifyMgr!!.notify(2, builder.build())
        }
    }

    override fun onProgressUpdate(vararg values: String) {
        super.onProgressUpdate(*values)
        if (!quiet) {
            builder.setContentTitle(values[0])
            builder.setWhen(System.currentTimeMillis())
            mNotifyMgr!!.notify(2, builder.build())
        }
    }

    override fun doInBackground(vararg params: Void): Boolean? {
        val ioHelper = IoHelper(mContext!!.context)
        publishProgress(mContext.context.getString(R.string.syncing_groups))
        ioHelper.restoreGroup(true)
        val list = AppDb.getAppDatabase(mContext.context).groupDao().all
        if (list.size == 0) {
            val defUiID = GroupsUtil.initDefault(mContext.context)
            val items = AppDb.getAppDatabase(mContext.context).reminderDao().all
            for (item in items) {
                item.groupUuId = defUiID
            }
            AppDb.getAppDatabase(mContext.context).reminderDao().insertAll(items)
        }
        ioHelper.backupGroup()

        //export & import reminders
        publishProgress(mContext.context.getString(R.string.syncing_reminders))
        ioHelper.restoreReminder(true)
        ioHelper.backupReminder()

        //export & import notes
        publishProgress(mContext.context.getString(R.string.syncing_notes))
        ioHelper.restoreNote(true)
        ioHelper.backupNote()

        //export & import birthdays
        publishProgress(mContext.context.getString(R.string.syncing_birthdays))
        ioHelper.restoreBirthday(true)
        ioHelper.backupBirthday()

        //export & import places
        publishProgress(mContext.context.getString(R.string.syncing_places))
        ioHelper.restorePlaces(true)
        ioHelper.backupPlaces()

        //export & import templates
        publishProgress(mContext.context.getString(R.string.syncing_templates))
        ioHelper.restoreTemplates(true)
        ioHelper.backupTemplates()
        ioHelper.backupSettings()
        return true
    }

    override fun onPostExecute(aVoid: Boolean?) {
        super.onPostExecute(aVoid)
        if (!quiet) {
            builder.setContentTitle(mContext!!.context.getString(R.string.done))
            if (Module.isLollipop) {
                builder.setSmallIcon(R.drawable.ic_done_white_24dp)
            } else {
                builder.setSmallIcon(R.drawable.ic_done_nv_white)
            }
            if (Module.isPro) {
                builder.setContentText(mContext.context.getString(R.string.app_name_pro))
            } else {
                builder.setContentText(mContext.context.getString(R.string.app_name))
            }
            builder.setWhen(System.currentTimeMillis())
            mNotifyMgr!!.notify(2, builder.build())
            if (mListener != null && mContext != null) {
                mListener.endExecution(aVoid!!)
            }
        }
        if (mContext != null) {
            UpdatesHelper.getInstance(mContext.context).updateWidget()
            UpdatesHelper.getInstance(mContext.context).updateNotesWidget()
        }
    }

    interface SyncListener {
        fun endExecution(b: Boolean)
    }
}
