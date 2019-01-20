package com.elementary.tasks.core.viewModels.googleTasks

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elementary.tasks.core.appWidgets.UpdatesHelper
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.viewModels.Commands
import com.google.api.services.tasks.model.TaskLists
import timber.log.Timber
import java.io.IOException
import java.util.*

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
class GoogleTaskListViewModel(application: Application, listId: String?) : BaseTaskListsViewModel(application) {

    var googleTaskList: LiveData<GoogleTaskList>
    var defaultTaskList: LiveData<GoogleTaskList>
    var googleTasks: LiveData<List<GoogleTask>>
    private var isSyncing = false

    init {
        defaultTaskList = appDb.googleTaskListsDao().loadDefault()
        Timber.d("GoogleTaskListViewModel: $listId")
        if (listId == null || listId == "") {
            googleTasks = appDb.googleTasksDao().loadAll()
            googleTaskList = appDb.googleTaskListsDao().loadById("")
        } else {
            googleTaskList = appDb.googleTaskListsDao().loadById(listId)
            googleTasks = appDb.googleTasksDao().loadAllByList(listId)
        }
    }

    fun sync() {
        val google = GTasks.getInstance(getApplication())
        if (google == null) {
            postCommand(Commands.FAILED)
            return
        }
        if (isSyncing) return
        isSyncing = true
        postInProgress(true)
        launchDefault {
            var lists: TaskLists? = null
            try {
                lists = google.taskLists()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            if (lists != null && lists.size > 0 && lists.items != null) {
                for (item in lists.items) {
                    val listId = item.id
                    var taskList = appDb.googleTaskListsDao().getById(listId)
                    if (taskList != null) {
                        taskList.update(item)
                    } else {
                        val r = Random()
                        val color = r.nextInt(15)
                        taskList = GoogleTaskList(item, color)
                    }
                    appDb.googleTaskListsDao().insert(taskList)
                    val tasks = google.getTasks(listId)
                    if (tasks.isEmpty()) {
                        withUIContext {
                            postInProgress(false)
                            postCommand(Commands.UPDATED)
                            UpdatesHelper.updateTasksWidget(getApplication())
                        }
                    } else {
                        val googleTasks = ArrayList<GoogleTask>()
                        for (task in tasks) {
                            var googleTask = appDb.googleTasksDao().getById(task.id)
                            if (googleTask != null) {
                                googleTask.listId = listId
                                googleTask.update(task)
                            } else {
                                googleTask = GoogleTask(task, listId)
                            }
                            googleTasks.add(googleTask)
                        }
                        appDb.googleTasksDao().insertAll(googleTasks)
                        withUIContext {
                            postInProgress(false)
                            postCommand(Commands.UPDATED)
                            UpdatesHelper.updateTasksWidget(getApplication())
                        }
                    }
                }
            }
            isSyncing = false
        }
    }

    fun newGoogleTaskList(googleTaskList: GoogleTaskList) {
        val google = GTasks.getInstance(getApplication())
        if (google == null) {
            postCommand(Commands.FAILED)
            return
        }
        postInProgress(true)
        launchDefault {
            google.insertTasksList(googleTaskList.title, googleTaskList.color)
            withUIContext {
                postInProgress(false)
                postCommand(Commands.SAVED)
            }
        }
    }

    fun updateGoogleTaskList(googleTaskList: GoogleTaskList) {
        val google = GTasks.getInstance(getApplication())
        if (google == null) {
            postCommand(Commands.FAILED)
            return
        }
        postInProgress(true)
        launchDefault {
            appDb.googleTaskListsDao().insert(googleTaskList)
            try {
                google.updateTasksList(googleTaskList.title, googleTaskList.listId)
                withUIContext {
                    postInProgress(false)
                    postCommand(Commands.SAVED)
                }
            } catch (e: IOException) {
                withUIContext {
                    postInProgress(false)
                    postCommand(Commands.FAILED)
                }
            }
        }
    }

    fun saveLocalGoogleTaskList(googleTaskList: GoogleTaskList) {
        postInProgress(true)
        launchDefault {
            appDb.googleTaskListsDao().insert(googleTaskList)
            withUIContext {
                postInProgress(false)
                postCommand(Commands.SAVED)
            }
        }
    }

    class Factory(private val application: Application, private val id: String?) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GoogleTaskListViewModel(application, id) as T
        }
    }
}
