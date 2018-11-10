package com.elementary.tasks.core.viewModels.reminders

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.elementary.tasks.R
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.reminder.work.DeleteBackupWorker

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
class ArchiveRemindersViewModel(application: Application) : BaseRemindersViewModel(application) {

    var events: LiveData<List<Reminder>>

    init {
        events = appDb.reminderDao().loadByRemoved(true)
    }

    fun deleteAll(data: List<Reminder>) {
        isInProgress.postValue(true)
        launchDefault {
            for (reminder in data) EventControlFactory.getController(reminder).stop()
            appDb.reminderDao().deleteAll(*data.toTypedArray())
            withUIContext {
                isInProgress.postValue(false)
                result.postValue(Commands.DELETED)
                Toast.makeText(getApplication(), R.string.trash_cleared, Toast.LENGTH_SHORT).show()
            }
            val work = OneTimeWorkRequest.Builder(DeleteBackupWorker::class.java)
                    .setInputData(Data.Builder().putStringArray(Constants.INTENT_IDS, data.map { it.uuId }.toTypedArray()).build())
                    .addTag("RM_WORK")
                    .build()
            WorkManager.getInstance().enqueue(work)
        }
    }
}
