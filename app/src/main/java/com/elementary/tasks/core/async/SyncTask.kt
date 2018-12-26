package com.elementary.tasks.core.async

import android.content.Context
import android.os.AsyncTask
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.appWidgets.UpdatesHelper
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.IoHelper
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.groups.GroupsUtil
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

class SyncTask(private val mListener: SyncListener?, private val quiet: Boolean) : AsyncTask<Void, String, Boolean>() {

    @Inject lateinit var context: Context
    @Inject lateinit var ioHelper: IoHelper
    @Inject lateinit var updatesHelper: UpdatesHelper
    private var mNotifyMgr: NotificationManagerCompat? = null
    private val builder: NotificationCompat.Builder

    init {
        builder = NotificationCompat.Builder(context, Notifier.CHANNEL_SYSTEM)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        if (!quiet) {
            builder.setContentTitle(if (Module.isPro)
                context.getString(R.string.app_name_pro)
            else
                context.getString(R.string.app_name))
            builder.setContentText(context.getString(R.string.sync))
            mNotifyMgr = NotificationManagerCompat.from(context)
            mNotifyMgr?.notify(2, builder.build())
        }
    }

    override fun onProgressUpdate(vararg values: String) {
        super.onProgressUpdate(*values)
        if (!quiet) {
            builder.setContentTitle(values[0])
            builder.setWhen(System.currentTimeMillis())
            mNotifyMgr?.notify(2, builder.build())
        }
    }

    override fun doInBackground(vararg params: Void): Boolean? {
        publishProgress(context.getString(R.string.syncing_groups))
        ioHelper.restoreGroup(true)
        val list = AppDb.getAppDatabase(context).reminderGroupDao().all()
        if (list.isEmpty()) {
            val defUiID = GroupsUtil.initDefault(context)
            val items = AppDb.getAppDatabase(context).reminderDao().all()
            for (item in items) {
                item.groupUuId = defUiID
            }
            AppDb.getAppDatabase(context).reminderDao().insertAll(items)
        }
        ioHelper.backupGroup()

        //export & import reminders
        publishProgress(context.getString(R.string.syncing_reminders))
        ioHelper.restoreReminder(true)
        ioHelper.backupReminder()

        //export & import notes
        publishProgress(context.getString(R.string.syncing_notes))
        ioHelper.restoreNote(true)
        ioHelper.backupNote()

        //export & import birthdays
        publishProgress(context.getString(R.string.syncing_birthdays))
        ioHelper.restoreBirthday(true)
        ioHelper.backupBirthday()

        //export & import places
        publishProgress(context.getString(R.string.syncing_places))
        ioHelper.restorePlaces(true)
        ioHelper.backupPlaces()

        //export & import templates
        publishProgress(context.getString(R.string.syncing_templates))
        ioHelper.restoreTemplates(true)
        ioHelper.backupTemplates()
        ioHelper.backupSettings()
        return true
    }

    override fun onPostExecute(aVoid: Boolean?) {
        super.onPostExecute(aVoid)
        if (!quiet) {
            builder.setContentTitle(context.getString(R.string.done))
            builder.setWhen(System.currentTimeMillis())
            mNotifyMgr?.notify(2, builder.build())
            mListener?.endExecution(aVoid!!)
        }
        updatesHelper.updateWidget()
        updatesHelper.updateNotesWidget()
    }

    interface SyncListener {
        fun endExecution(b: Boolean)
    }
}
