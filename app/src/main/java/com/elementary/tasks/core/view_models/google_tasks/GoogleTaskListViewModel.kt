package com.elementary.tasks.core.view_models.google_tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.view_models.Commands
import com.google.api.services.tasks.model.TaskLists
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.io.IOException
import java.util.*

class GoogleTaskListViewModel(listId: String) : BaseTaskListsViewModel() {

    var googleTaskList: LiveData<GoogleTaskList>
    val defaultTaskList = appDb.googleTaskListsDao().loadDefault()
    var googleTasks: LiveData<List<GoogleTask>>
    private var isSyncing = false

    init {
        Timber.d("GoogleTaskListViewModel: $listId")
        googleTaskList = appDb.googleTaskListsDao().loadById(listId)
        googleTasks = appDb.googleTasksDao().loadAllByList(listId)
    }

    fun sync() {
        val google = GTasks.getInstance(context)
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
                            UpdatesHelper.updateTasksWidget(context)
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
                            UpdatesHelper.updateTasksWidget(context)
                        }
                    }
                }
            }
            isSyncing = false
        }
    }

    fun newGoogleTaskList(googleTaskList: GoogleTaskList) {
        val google = GTasks.getInstance(context)
        if (google == null) {
            postCommand(Commands.FAILED)
            return
        }
        postInProgress(true)
        launchDefault {
            google.insertTasksList(googleTaskList.title, googleTaskList.color)
            postInProgress(false)
            postCommand(Commands.SAVED)
        }
    }

    fun updateGoogleTaskList(googleTaskList: GoogleTaskList) {
        val google = GTasks.getInstance(context)
        if (google == null) {
            postCommand(Commands.FAILED)
            return
        }
        postInProgress(true)
        launchDefault {
            appDb.googleTaskListsDao().insert(googleTaskList)
            try {
                google.updateTasksList(googleTaskList.title, googleTaskList.listId)
                postInProgress(false)
                postCommand(Commands.SAVED)
            } catch (e: IOException) {
                postInProgress(false)
                postCommand(Commands.FAILED)
            }
        }
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
            postInProgress(false)
            postCommand(Commands.UPDATED)
            withUIContext {
                UpdatesHelper.updateTasksWidget(context)
            }
        }
    }

    fun saveLocalGoogleTaskList(googleTaskList: GoogleTaskList) {
        postInProgress(true)
        launchDefault {
            appDb.googleTaskListsDao().insert(googleTaskList)
            postInProgress(false)
            postCommand(Commands.SAVED)
        }
    }

    class Factory(private val id: String) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GoogleTaskListViewModel(id) as T
        }
    }
}
