package com.elementary.tasks.core.viewModels.groups

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.toWorkData
import com.elementary.tasks.core.data.models.Group
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.groups.work.SingleBackupWorker
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch

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
class GroupViewModel private constructor(application: Application, id: String) : BaseGroupsViewModel(application) {

    var group: LiveData<Group>

    init {
        group = appDb.groupDao().loadById(id)
    }

    fun saveGroup(group: Group) {
        isInProgress.postValue(true)
        launch(CommonPool) {
            appDb.groupDao().insert(group)
            val work = OneTimeWorkRequest.Builder(SingleBackupWorker::class.java)
                    .setInputData(mapOf(Constants.INTENT_ID to group.uuId).toWorkData())
                    .addTag(group.uuId)
                    .build()
            WorkManager.getInstance().enqueue(work)
            withUIContext {
                isInProgress.postValue(false)
                result.postValue(Commands.SAVED)
            }
        }
    }

    class Factory(private val application: Application, private val id: String) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GroupViewModel(application, id) as T
        }
    }
}
