package com.elementary.tasks.core.viewModels.reminders

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup

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
class ReminderViewModel private constructor(application: Application, id: String) : BaseRemindersViewModel(application) {

    var reminder: LiveData<Reminder>
    val reminderGroup: MutableLiveData<ReminderGroup> = MutableLiveData()

    init {
        reminder = appDb.reminderDao().loadById(id)
    }

    fun loadGroup(uuId: String) {
        reminderGroup.postValue(appDb.reminderGroupDao().getById(uuId))
    }

    class Factory(private val application: Application, private val id: String) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReminderViewModel(application, id) as T
        }
    }
}
