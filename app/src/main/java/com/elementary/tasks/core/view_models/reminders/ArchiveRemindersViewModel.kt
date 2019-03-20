package com.elementary.tasks.core.view_models.reminders

import androidx.lifecycle.LiveData
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.reminder.work.DeleteBackupWorker
import kotlinx.coroutines.runBlocking

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
class ArchiveRemindersViewModel : BaseRemindersViewModel() {

    var events: LiveData<List<Reminder>>

    init {
        events = appDb.reminderDao().loadNotRemoved(true)
    }

    fun deleteAll(data: List<Reminder>) {
        postInProgress(true)
        launchDefault {
            runBlocking {
                data.forEach {
                    EventControlFactory.getController(it).stop()
                }
                appDb.reminderDao().deleteAll(data)
            }
            data.forEach {
                startWork(DeleteBackupWorker::class.java, Constants.INTENT_ID, it.uuId)
            }
            withUIContext {
                postInProgress(false)
                postCommand(Commands.DELETED)
            }
        }
    }
}
