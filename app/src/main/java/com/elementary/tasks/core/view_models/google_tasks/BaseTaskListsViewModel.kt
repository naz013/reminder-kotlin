package com.elementary.tasks.core.view_models.google_tasks

import android.content.Context
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.core.view_models.Commands
import kotlinx.coroutines.runBlocking
import java.io.IOException
import javax.inject.Inject

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
abstract class BaseTaskListsViewModel : BaseDbViewModel() {

    @Inject
    lateinit var context: Context

    init {
        ReminderApp.appComponent.inject(this)
    }

    fun deleteGoogleTaskList(googleTaskList: GoogleTaskList) {
        val google = GTasks.getInstance(context)
        if (google == null) {
            postCommand(Commands.FAILED)
            return
        }
        postInProgress(true)
        launchDefault {
            runBlocking {
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
            }
            withUIContext {
                postInProgress(false)
                postCommand(Commands.DELETED)
            }
        }
    }

    fun toggleTask(googleTask: GoogleTask) {
        val google = GTasks.getInstance(context)
        if (google == null) {
            postCommand(Commands.FAILED)
            return
        }
        postInProgress(true)
        launchDefault {
            try {
                runBlocking {
                    if (googleTask.status == GTasks.TASKS_NEED_ACTION) {
                        google.updateTaskStatus(GTasks.TASKS_COMPLETE, googleTask)
                    } else {
                        google.updateTaskStatus(GTasks.TASKS_NEED_ACTION, googleTask)
                    }
                }
                withUIContext {
                    postInProgress(false)
                    postCommand(Commands.UPDATED)
                    UpdatesHelper.updateTasksWidget(context)
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
