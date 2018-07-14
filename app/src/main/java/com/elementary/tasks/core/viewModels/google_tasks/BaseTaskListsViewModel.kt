package com.elementary.tasks.core.viewModels.google_tasks

import android.app.Application

import com.elementary.tasks.core.appWidgets.UpdatesHelper
import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.SuperUtil
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
internal abstract class BaseTaskListsViewModel(application: Application) : BaseDbViewModel(application) {

    fun deleteGoogleTaskList(googleTaskList: GoogleTaskList) {
        val google = Google.getInstance(getApplication())
        if (google == null || google.tasks == null) {
            return
        }
        val isConnected = SuperUtil.isConnected(getApplication())
        if (!isConnected) {
            result.postValue(Commands.FAILED)
            return
        }
        isInProgress.postValue(true)
        run {
            val def = googleTaskList.def
            google.tasks!!.deleteTaskList(googleTaskList.listId)
            appDb!!.googleTaskListsDao().delete(googleTaskList)
            appDb!!.googleTasksDao().deleteAll(googleTaskList.listId)
            if (def == 1) {
                val lists = appDb!!.googleTaskListsDao().all
                if (!lists.isEmpty()) {
                    val taskList = lists[0]
                    taskList.def = 1
                    appDb!!.googleTaskListsDao().insert(taskList)
                }
            }
            end {
                isInProgress.postValue(false)
                result.postValue(Commands.DELETED)
            }
        }
    }

    fun toggleTask(googleTask: GoogleTask) {
        val mGoogle = Google.getInstance(getApplication())
        if (mGoogle == null || mGoogle.tasks == null) {
            return
        }
        val isConnected = SuperUtil.isConnected(getApplication())
        if (!isConnected) {
            result.postValue(Commands.FAILED)
        } else {
            isInProgress.postValue(true)
            run {
                try {
                    if (googleTask.status == Google.TASKS_NEED_ACTION) {
                        mGoogle.tasks!!.updateTaskStatus(Google.TASKS_COMPLETE, googleTask.listId, googleTask.taskId)
                    } else {
                        mGoogle.tasks!!.updateTaskStatus(Google.TASKS_NEED_ACTION, googleTask.listId, googleTask.taskId)
                    }
                    end {
                        isInProgress.postValue(false)
                        result.postValue(Commands.UPDATED)
                        UpdatesHelper.getInstance(getApplication()).updateTasksWidget()
                    }
                } catch (e: IOException) {
                    end {
                        isInProgress.postValue(false)
                        result.postValue(Commands.FAILED)
                    }
                }
            }
        }
    }
}
