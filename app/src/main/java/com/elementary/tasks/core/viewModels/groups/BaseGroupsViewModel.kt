package com.elementary.tasks.core.viewModels.groups

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.toWorkData
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.viewModels.BaseDbViewModel
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.groups.work.DeleteBackupWorker
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

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
        launch(CommonPool) {
            appDb.reminderGroupDao().delete(reminderGroup)
            withContext(UI) {
                isInProgress.postValue(false)
                result.postValue(Commands.DELETED)
            }
            val work = OneTimeWorkRequest.Builder(DeleteBackupWorker::class.java)
                    .setInputData(mapOf(Constants.INTENT_ID to reminderGroup.uuId).toWorkData())
                    .addTag(reminderGroup.uuId)
                    .build()
            WorkManager.getInstance().enqueue(work)
        }
    }
}
