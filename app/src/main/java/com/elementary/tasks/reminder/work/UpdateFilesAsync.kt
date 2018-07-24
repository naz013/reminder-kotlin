package com.elementary.tasks.reminder.work

import android.content.Context
import android.os.AsyncTask

import com.elementary.tasks.core.cloud.Dropbox
import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.data.models.Reminder

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
class UpdateFilesAsync(context: Context) : AsyncTask<Reminder, Void, Void>() {

    private val isConnected: Boolean = SuperUtil.isConnected(context)
    private val dropbox: Dropbox = Dropbox()
    private val google: Google? = Google.getInstance()

    override fun doInBackground(vararg params: Reminder): Void? {
        for (reminder in params) {
            val path = BackupTool.getInstance().exportReminder(reminder)
            if (isConnected) {
                dropbox.uploadReminderByFileName(path)
                if (google?.drive != null && path != null) {
                    google.drive?.saveReminderToDrive(path)
                }
            }
        }
        return null
    }
}
