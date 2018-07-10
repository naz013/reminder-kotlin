package com.elementary.tasks.google_tasks

import android.content.Context
import android.os.AsyncTask

import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.google.api.services.tasks.model.Task
import com.google.api.services.tasks.model.TaskList
import com.google.api.services.tasks.model.TaskLists

import java.io.IOException
import java.lang.ref.WeakReference
import java.util.Random

/**
 * Copyright 2016 Nazar Suhovich
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

class GetTaskListAsync(context: Context, private val mListener: TasksCallback?) : AsyncTask<Void, Void, Boolean>() {
    private val mGoogle: Google?
    private val mHelper: WeakReference<UpdatesHelper>?
    private val appDb: AppDb

    init {
        this.mGoogle = Google.getInstance(context)
        this.appDb = AppDb.getAppDatabase(context)
        this.mHelper = WeakReference(UpdatesHelper.getInstance(context))
    }

    override fun doInBackground(vararg params: Void): Boolean? {
        if (mGoogle != null && mGoogle.tasks != null) {
            var lists: TaskLists? = null
            try {
                lists = mGoogle.tasks!!.taskLists
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
                    val listItem = appDb.googleTaskListsDao().all[0]
                    listItem.def = 1
                    listItem.systemDefault = 1
                    appDb.googleTaskListsDao().insert(listItem)
                    val tasks = mGoogle.tasks!!.getTasks(listId)
                    if (tasks.isEmpty()) return false
                    for (task in tasks) {
                        var googleTask = appDb.googleTasksDao().getById(task.id)
                        if (googleTask != null) {
                            googleTask.update(task)
                            googleTask.listId = task.id
                        } else {
                            googleTask = GoogleTask(task, listId)
                        }
                        appDb.googleTasksDao().insert(googleTask)
                    }
                }
            }
            return true
        }
        return false
    }

    override fun onPostExecute(aVoid: Boolean?) {
        super.onPostExecute(aVoid)
        if (mHelper != null) {
            val helper = mHelper.get()
            helper?.updateTasksWidget()
        }
        if (mListener != null) {
            if (aVoid!!) {
                mListener.onComplete()
            } else {
                mListener.onFailed()
            }
        }
    }
}
