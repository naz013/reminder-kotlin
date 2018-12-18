package com.elementary.tasks.core.viewModels.googleTasks

import android.app.Application
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.viewModels.BaseDbViewModel
import com.elementary.tasks.core.viewModels.Commands
import java.io.IOException

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
abstract class BaseTaskListsViewModel(application: Application) : BaseDbViewModel(application) {

    fun deleteGoogleTaskList(googleTaskList: GoogleTaskList) {
        val google = GTasks.getInstance(getApplication())
        if (google == null) {
            postCommand(Commands.FAILED)
            return
        }
        val isConnected = SuperUtil.isConnected(getApplication())
        if (!isConnected) {
            postCommand(Commands.FAILED)
            return
        }
        postInProgress(true)
        launchDefault {
            val def = googleTaskList.def
            google.deleteTaskList(googleTaskList.listId)
            appDb.googleTaskListsDao().delete(googleTaskList)
            appDb.googleTasksDao().deleteAll(googleTaskList.listId)
            if (def == 1) {
                val lists = appDb.googleTaskListsDao().all()
                if (lists.isNotEmpty()) {
                    val taskList = lists[0]
                    taskList.def = 1
                    appDb.googleTaskListsDao().insert(taskList)
                }
            }
            withUIContext {
                postInProgress(false)
                postCommand(Commands.DELETED)
            }
        }
    }

    fun toggleTask(googleTask: GoogleTask) {
        val google = GTasks.getInstance(getApplication())
        if (google == null) {
            postCommand(Commands.FAILED)
            return
        }
        val isConnected = SuperUtil.isConnected(getApplication())
        if (!isConnected) {
            postCommand(Commands.FAILED)
        } else {
            postInProgress(true)
            launchDefault {
                try {
                    if (googleTask.status == GTasks.TASKS_NEED_ACTION) {
                        google.updateTaskStatus(GTasks.TASKS_COMPLETE, googleTask)
                    } else {
                        google.updateTaskStatus(GTasks.TASKS_NEED_ACTION, googleTask)
                    }
                    withUIContext {
                        postInProgress(false)
                        postCommand(Commands.UPDATED)
                        updatesHelper.updateTasksWidget()
                    }
                } catch (e: IOException) {
                    withUIContext {
                        postInProgress(false)
                        postCommand(Commands.FAILED)
                    }
                }
            }
        }
    }
}
