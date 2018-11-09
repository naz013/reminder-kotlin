package com.elementary.tasks.core.viewModels.groups

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.viewModels.BaseDbViewModel
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.groups.work.DeleteBackupWorker

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
abstract class BaseGroupsViewModel(application: Application) : BaseDbViewModel(application) {

    var allGroups: LiveData<List<ReminderGroup>>

    init {
        allGroups = appDb.reminderGroupDao().loadAll()
    }

    fun deleteGroup(reminderGroup: ReminderGroup) {
        isInProgress.postValue(true)
        launchDefault {
            appDb.reminderGroupDao().delete(reminderGroup)
            withUIContext {
                isInProgress.postValue(false)
                result.postValue(Commands.DELETED)
            }
            val work = OneTimeWorkRequest.Builder(DeleteBackupWorker::class.java)
                    .setInputData(Data.Builder().putString(Constants.INTENT_ID, reminderGroup.groupUuId).build())
                    .addTag(reminderGroup.groupUuId)
                    .build()
            WorkManager.getInstance().enqueue(work)
        }
    }
}
