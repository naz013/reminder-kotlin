package com.elementary.tasks.core.view_models.google_tasks

import androidx.lifecycle.LiveData
import com.elementary.tasks.core.appWidgets.UpdatesHelper
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.view_models.Commands
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
class GoogleTaskListsViewModel : BaseTaskListsViewModel() {

    var googleTaskLists: LiveData<List<GoogleTaskList>>
    var defaultTaskList: LiveData<GoogleTaskList>

    init {
        defaultTaskList = appDb.googleTaskListsDao().loadDefault()
        googleTaskLists = appDb.googleTaskListsDao().loadAll()
    }

    fun clearList(googleTaskList: GoogleTaskList) {
        val google = GTasks.getInstance(context)
        if (google == null) {
            postCommand(Commands.FAILED)
            return
        }
        postInProgress(true)
        launchDefault {
            runBlocking {
                val googleTasks = appDb.googleTasksDao().getAllByList(googleTaskList.listId, GTasks.TASKS_COMPLETE)
                appDb.googleTasksDao().deleteAll(googleTasks)
                google.clearTaskList(googleTaskList.listId)
            }
            withUIContext {
                postInProgress(false)
                postCommand(Commands.UPDATED)
                UpdatesHelper.updateTasksWidget(context)
            }
        }
    }
}
