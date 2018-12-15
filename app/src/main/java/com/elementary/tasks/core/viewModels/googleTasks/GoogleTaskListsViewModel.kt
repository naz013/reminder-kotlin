package com.elementary.tasks.core.viewModels.googleTasks

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.googleTasks.GoogleTaskComposed
import com.google.api.services.tasks.model.TaskLists
import kotlinx.coroutines.Job
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
class GoogleTaskListsViewModel(application: Application) : BaseTaskListsViewModel(application) {

    private var liveData: PageLiveData

    var googleTaskLists: LiveData<List<GoogleTaskList>>

    private val _googleTasks: MutableLiveData<Pair<String, GoogleTaskComposed>> = MutableLiveData()
    val googleTasks: LiveData<Pair<String, GoogleTaskComposed>> = _googleTasks

    init {
        googleTaskLists = appDb.googleTaskListsDao().loadAll()
        liveData = PageLiveData()
    }

    fun findTasks(listId: String) {
        try {
            liveData.findTasks(listId) { id, composed ->
                _googleTasks.postValue(Pair(id, composed))
            }
        } catch (e: UninitializedPropertyAccessException) {
        }
    }

    fun sync() {
        val isConnected = SuperUtil.isConnected(getApplication())
        if (!isConnected) {
            Commands.FAILED.post()
            return
        }
        val google = GTasks.getInstance(getApplication())
        if (google == null) {
            Commands.FAILED.post()
            return
        }
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
                            Commands.UPDATED.post()
                            updatesHelper.updateTasksWidget()
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
                            Commands.UPDATED.post()
                            updatesHelper.updateTasksWidget()
                        }
                    }
                }
            }
        }
    }

    fun reload() {

    }

    fun clearList(googleTaskList: GoogleTaskList) {
        val google = GTasks.getInstance(getApplication())
        if (google == null) {
            Commands.FAILED.post()
            return
        }
        val isConnected = SuperUtil.isConnected(getApplication())
        if (!isConnected) {
            Commands.FAILED.post()
        } else {
            postInProgress(true)
            launchDefault {
                val googleTasks = appDb.googleTasksDao().getAllByList(googleTaskList.listId, GTasks.TASKS_COMPLETE)
                appDb.googleTasksDao().deleteAll(googleTasks)
                google.clearTaskList(googleTaskList.listId)
                withUIContext {
                    postInProgress(false)
                    Commands.UPDATED.post()
                    updatesHelper.updateTasksWidget()
                }
            }
        }
    }

    private inner class PageLiveData internal constructor() : LiveData<Pair<String, GoogleTaskComposed>>() {

        private val taskListsData = ArrayList<GoogleTaskList>()
        private val tasksData = ArrayList<GoogleTask>()
        private val taskLists = appDb.googleTaskListsDao().loadAll()
        private val tasks = appDb.googleTasksDao().loadAll()

        private var listId: String? = null
        private var job: Job? = null
        private var listener: ((String, GoogleTaskComposed) -> Unit)? = null

        private val taskListsDataObserver: Observer<in List<GoogleTaskList>> = Observer {
            Timber.d("taskListsChanged: ")
            launchDefault {
                if (it != null) {
                    taskListsData.clear()
                    taskListsData.addAll(it)
                    repeatSearch()
                }
            }
        }
        private val tasksDataObserver: Observer<in List<GoogleTask>> = Observer {
            Timber.d("tasksChanged: ")
            launchDefault {
                if (it != null) {
                    tasksData.clear()
                    tasksData.addAll(it)
                    repeatSearch()
                }
            }
        }

        init {
            taskLists.observeForever(taskListsDataObserver)
            tasks.observeForever(tasksDataObserver)
        }

        fun findTasks(listId: String, listener: ((String, GoogleTaskComposed) -> Unit)?) {
            if (listener == null) return
            this.listener = listener
            this.listId = listId
            findMatches(taskListsData, tasksData, listId)
        }

        override fun onInactive() {
            super.onInactive()
            Timber.d("onInactive: ")
            taskLists.observeForever(taskListsDataObserver)
            tasks.observeForever(tasksDataObserver)
            this.listId = ""
        }

        override fun onActive() {
            super.onActive()
            Timber.d("onActive: ")
            taskLists.removeObserver(taskListsDataObserver)
            tasks.removeObserver(tasksDataObserver)
        }

        private fun notifyObserver(listId: String, googleTaskComposed: GoogleTaskComposed) {
            listener?.invoke(listId, googleTaskComposed)
        }

        private fun repeatSearch() {
            val item = listId ?: return
            findTasks(item, listener)
        }

        private fun findMatches(googleTaskLists: List<GoogleTaskList>, googleTasks: List<GoogleTask>, listId: String) {
            this.job?.cancel()
            this.job = launchDefault {
                val res = GoogleTaskComposed()
                res.listId = listId
                Timber.d("Search events: $listId, $googleTasks")
                if (listId == "") {
                    res.googleTasks = googleTasks
                } else {
                    for (googleTaskList in googleTaskLists) {
                        if (googleTaskList.listId == listId) {
                            val list: MutableList<GoogleTask> = mutableListOf()
                            for (task in googleTasks) {
                                if (task.listId == googleTaskList.listId) {
                                    list.add(task)
                                }
                            }
                            res.googleTasks = list
                            break
                        }
                    }
                }
                Timber.d("Search events: found -> $res")
                withUIContext { notifyObserver(listId, res) }
            }
        }
    }
}
