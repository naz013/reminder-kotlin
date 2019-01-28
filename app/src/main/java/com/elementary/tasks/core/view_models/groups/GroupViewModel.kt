package com.elementary.tasks.core.view_models.groups

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.groups.work.SingleBackupWorker
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
class GroupViewModel private constructor(id: String) : BaseGroupsViewModel() {

    var reminderGroup: LiveData<ReminderGroup>
    var isEdited = false

    init {
        reminderGroup = appDb.reminderGroupDao().loadById(id)
    }

    fun saveGroup(reminderGroup: ReminderGroup, wasDefault: Boolean) {
        postInProgress(true)
        launchDefault {
            runBlocking {
                if (!wasDefault && reminderGroup.isDefaultGroup) {
                    val groups = appDb.reminderGroupDao().all()
                    for (g in groups) g.isDefaultGroup = false
                    appDb.reminderGroupDao().insertAll(groups)
                }
                appDb.reminderGroupDao().insert(reminderGroup)
            }
            startWork(SingleBackupWorker::class.java, Constants.INTENT_ID, reminderGroup.groupUuId)
            withUIContext {
                postInProgress(false)
                postCommand(Commands.SAVED)
            }
        }
    }

    class Factory(private val id: String) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GroupViewModel(id) as T
        }
    }
}
