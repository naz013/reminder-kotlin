package com.elementary.tasks.core.view_models.reminders

import android.app.Application
import android.widget.Toast

import com.elementary.tasks.R
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.Group
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.reminder.work.DeleteFilesAsync

import androidx.lifecycle.LiveData

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
    var groups: LiveData<List<Group>>

    init {
        events = appDb!!.reminderDao().loadType(false, false)
        groups = appDb!!.groupDao().loadAll()
    }

    fun deleteAll(data: List<Reminder>) {
        isInProgress.postValue(true)
        run {
            for (reminder in data) EventControlFactory.getController(reminder).stop()
            appDb!!.reminderDao().deleteAll(*data.toTypedArray())
            end {
                isInProgress.postValue(false)
                result.postValue(Commands.DELETED)
                Toast.makeText(getApplication(), R.string.trash_cleared, Toast.LENGTH_SHORT).show()
            }
            DeleteFilesAsync(getApplication()).execute(*data.stream().map<String>(Function<Reminder, String> { it.getUuId() }).toArray(String[]::new  /* Currently unsupported in Kotlin */))
        }
    }
}
