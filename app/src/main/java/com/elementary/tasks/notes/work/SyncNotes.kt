package com.elementary.tasks.notes.work

import android.content.Context
import android.os.AsyncTask
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import com.elementary.tasks.R
import com.elementary.tasks.core.appWidgets.UpdatesHelper
import com.elementary.tasks.core.cloud.Dropbox
import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.SuperUtil

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

class SyncNotes(private val mContext: Context, private val mListener: ((Boolean) -> Unit)?) : AsyncTask<Void, Void, Boolean>() {
    private var mNotifyMgr: NotificationManagerCompat? = null
    private val builder: NotificationCompat.Builder = NotificationCompat.Builder(mContext, Notifier.CHANNEL_SYSTEM)

    override fun onPreExecute() {
        super.onPreExecute()
        builder.setContentTitle(mContext.getString(R.string.notes))
        builder.setContentText(mContext.getString(R.string.syncing_notes))
        if (Module.isLollipop) {
            builder.setSmallIcon(R.drawable.ic_cached_black_24dp)
        } else {
            builder.setSmallIcon(R.drawable.ic_cached_nv_white)
        }
        mNotifyMgr = NotificationManagerCompat.from(mContext)
        mNotifyMgr!!.notify(2, builder.build())
    }

    override fun doInBackground(vararg params: Void): Boolean {
        try {
            BackupTool.getInstance().importNotes()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

        BackupTool.getInstance().exportNotes()

        if (SuperUtil.isConnected(mContext)) {
            Dropbox(mContext).downloadNotes(true)
            Dropbox(mContext).uploadNotes()
            val google = Google.getInstance()
            if (google != null) {
                val drives = google.drive
                if (drives != null) {
                    try {
                        drives.downloadNotes(true)
                        drives.saveNotesToDrive()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
        }
        return true
    }

    override fun onPostExecute(aVoid: Boolean) {
        super.onPostExecute(aVoid)
        builder.setContentTitle(mContext.getString(R.string.done))
        if (Module.isLollipop) {
            builder.setSmallIcon(R.drawable.ic_done_white_24dp)
        } else {
            builder.setSmallIcon(R.drawable.ic_done_nv_white)
        }
        if (Module.isPro) {
            builder.setContentText(mContext.getString(R.string.app_name_pro))
        } else {
            builder.setContentText(mContext.getString(R.string.app_name))
        }
        builder.setWhen(System.currentTimeMillis())
        mNotifyMgr!!.notify(2, builder.build())
        mListener?.invoke(aVoid)
        UpdatesHelper.getInstance(mContext).updateNotesWidget()
    }
}
