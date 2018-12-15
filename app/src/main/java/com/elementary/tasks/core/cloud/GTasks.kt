package com.elementary.tasks.core.cloud

import android.content.Context
import android.text.TextUtils
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.Prefs
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.Data
import com.google.api.client.util.DateTime
import com.google.api.services.tasks.Tasks
import com.google.api.services.tasks.TasksScopes
import com.google.api.services.tasks.model.Task
import com.google.api.services.tasks.model.TaskList
import com.google.api.services.tasks.model.TaskLists
import java.io.IOException
import java.util.*
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
class GTasks private constructor(context: Context) {

    private var tasksService: Tasks? = null

    @Inject
    lateinit var appDb: AppDb
    @Inject
    lateinit var prefs: Prefs
    @Inject
    lateinit var backupTool: BackupTool

    var isLogged: Boolean = false
        private set

    init {
        ReminderApp.appComponent.inject(this)
        val user = prefs.tasksUser
        if (user.matches(".*@.*".toRegex())) {
            val credential = GoogleAccountCredential.usingOAuth2(context, Arrays.asList(TasksScopes.TASKS))
            credential.selectedAccountName = user
            val mJsonFactory = GsonFactory.getDefaultInstance()
            val mTransport = AndroidHttp.newCompatibleTransport()
            tasksService = Tasks.Builder(mTransport, mJsonFactory, credential).setApplicationName(APPLICATION_NAME).build()
            isLogged = true
        } else {
            logOut()
        }
    }

    internal fun logOut() {
        prefs.tasksUser = Prefs.DRIVE_USER_NONE
        instance = null
        isLogged = false
    }

    @Throws(IOException::class)
    fun taskLists(): TaskLists? {
        return if (!isLogged || tasksService == null) null else tasksService?.tasklists()?.list()?.execute()
    }

    @Throws(IOException::class)
    fun insertTask(item: GoogleTask): Boolean {
        if (!isLogged || TextUtils.isEmpty(item.title) || tasksService == null) {
            return false
        }
        try {
            val task = Task()
            task.title = item.title
            if (item.notes != "") {
                task.notes = item.notes
            }
            if (item.dueDate != 0L) {
                task.due = DateTime(item.dueDate)
            }
            val result: Task?
            val listId = item.listId
            if (!TextUtils.isEmpty(listId)) {
                result = tasksService?.tasks()?.insert(listId, task)?.execute()
            } else {
                val googleTaskList = appDb.googleTaskListsDao().defaultGoogleTaskList()
                if (googleTaskList != null) {
                    item.listId = googleTaskList.listId
                    result = tasksService?.tasks()?.insert(googleTaskList.listId, task)?.execute()
                } else {
                    result = tasksService?.tasks()?.insert("@default", task)?.execute()
                    val list = tasksService?.tasklists()?.get("@default")?.execute()
                    if (list != null) {
                        item.listId = list.id
                    }
                }
            }
            if (result != null) {
                item.update(result)
                appDb.googleTasksDao().insert(item)
                return true
            }
        } catch (e: IllegalArgumentException) {
            return false
        }
        return false
    }

    @Throws(IOException::class)
    fun updateTaskStatus(status: String, listId: String, taskId: String) {
        if (!isLogged || tasksService == null) return
        val task = tasksService?.tasks()?.get(listId, taskId)?.execute() ?: return
        task.status = status
        if (status.matches(TASKS_NEED_ACTION.toRegex())) {
            task.completed = Data.NULL_DATE_TIME
        }
        task.updated = DateTime(System.currentTimeMillis())
        tasksService?.tasks()?.update(listId, task.id, task)?.execute()
    }

    @Throws(IOException::class)
    fun deleteTask(item: GoogleTask) {
        if (!isLogged || item.listId == "" || tasksService == null) return
        tasksService?.tasks()?.delete(item.listId, item.taskId)?.execute()
    }

    @Throws(IOException::class)
    fun updateTask(item: GoogleTask) {
        if (!isLogged || tasksService == null) return
        val task = tasksService?.tasks()?.get(item.listId, item.taskId)?.execute() ?: return
        task.status = TASKS_NEED_ACTION
        task.title = item.title
        task.completed = Data.NULL_DATE_TIME
        if (item.dueDate != 0L) task.due = DateTime(item.dueDate)
        if (item.notes != "") task.notes = item.notes
        task.updated = DateTime(System.currentTimeMillis())
        tasksService?.tasks()?.update(item.listId, task.id, task)?.execute()
    }

    fun getTasks(listId: String): List<Task> {
        var taskLists: List<Task> = ArrayList()
        if (!isLogged || tasksService == null) return taskLists
        try {
            taskLists = tasksService?.tasks()?.list(listId)?.execute()?.items ?: arrayListOf()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return taskLists
    }

    fun insertTasksList(listTitle: String, color: Int) {
        if (!isLogged || tasksService == null) return
        val taskList = TaskList()
        taskList.title = listTitle
        try {
            val result = tasksService?.tasklists()?.insert(taskList)?.execute() ?: return
            val item = GoogleTaskList(result, color)
            appDb.googleTaskListsDao().insert(item)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    @Throws(IOException::class)
    fun updateTasksList(listTitle: String, listId: String?) {
        if (!isLogged || listId == null || tasksService == null) {
            return
        }
        val taskList = tasksService?.tasklists()?.get(listId)?.execute() ?: return
        taskList.title = listTitle
        tasksService?.tasklists()?.update(listId, taskList)?.execute()
        val item = appDb.googleTaskListsDao().getById(listId)
        if (item != null) {
            item.update(taskList)
            appDb.googleTaskListsDao().insert(item)
        }
    }

    fun deleteTaskList(listId: String?) {
        if (!isLogged || listId == null || tasksService == null) {
            return
        }
        try {
            tasksService?.tasklists()?.delete(listId)?.execute()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun clearTaskList(listId: String?) {
        if (!isLogged || listId == null || tasksService == null) {
            return
        }
        try {
            tasksService?.tasks()?.clear(listId)?.execute()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun moveTask(item: GoogleTask, oldList: String): Boolean {
        if (!isLogged || tasksService == null) {
            return false
        }
        try {
            val task = tasksService?.tasks()?.get(oldList, item.taskId)?.execute()
            if (task != null) {
                val clone = GoogleTask(item)
                clone.listId = oldList
                deleteTask(clone)
                return insertTask(item)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    companion object {

        const val TASKS_NEED_ACTION = "needsAction"
        const val TASKS_COMPLETE = "completed"
        private const val TAG = "GTasks"
        private const val APPLICATION_NAME = "Reminder/6.0"

        private var instance: GTasks? = null

        fun getInstance(context: Context): GTasks? {
            if (instance == null) {
                instance = GTasks(context)
            }
            return instance
        }
    }
}
