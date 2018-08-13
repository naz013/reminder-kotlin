package com.elementary.tasks.core.viewModels.googleTasks

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.viewModels.Commands
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
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
class GoogleTaskViewModel(application: Application, id: String) : BaseTaskListsViewModel(application) {

    var googleTask: LiveData<GoogleTask>
    var defaultTaskList: LiveData<GoogleTaskList>
    var defaultReminderGroup: LiveData<ReminderGroup>
    var googleTaskLists: LiveData<List<GoogleTaskList>>
    var reminder = MutableLiveData<Reminder>()

    init {
        googleTask = appDb.googleTasksDao().loadById(id)
        defaultTaskList = appDb.googleTaskListsDao().loadDefault()
        googleTaskLists = appDb.googleTaskListsDao().loadAll()
        defaultReminderGroup = appDb.reminderGroupDao().loadDefault()
    }

    fun loadReminder(uuId: String) {
        isInProgress.postValue(true)
        launch(CommonPool) {
            val reminderItem = appDb.reminderDao().getById(uuId)
            withContext(UI) {
                reminder.postValue(reminderItem)
                isInProgress.postValue(false)
            }
        }
    }

    private fun saveReminder(reminder: Reminder?) {
        if (reminder != null) {
            appDb.reminderDao().insert(reminder)
            EventControlFactory.getController(reminder).start()
        }
    }

    fun deleteGoogleTask(googleTask: GoogleTask) {
        val google = Google.getInstance()
        if (google?.tasks == null) {
            return
        }
        val isConnected = SuperUtil.isConnected(getApplication())
        if (!isConnected) {
            result.postValue(Commands.FAILED)
            return
        }
        isInProgress.postValue(true)
        launch(CommonPool) {
            try {
                google.tasks?.deleteTask(googleTask)
                appDb.googleTasksDao().delete(googleTask)
                withContext(UI) {
                    isInProgress.postValue(false)
                    result.postValue(Commands.DELETED)
                }
            } catch (e: IOException) {
                withContext(UI) {
                    isInProgress.postValue(false)
                    result.postValue(Commands.FAILED)
                }
            }
        }
    }

    fun newGoogleTask(googleTask: GoogleTask, reminder: Reminder?) {
        val google = Google.getInstance()
        if (google?.tasks == null) {
            return
        }
        val isConnected = SuperUtil.isConnected(getApplication())
        if (!isConnected) {
            result.postValue(Commands.FAILED)
            return
        }
        isInProgress.postValue(true)
        launch(CommonPool) {
            try {
                google.tasks?.insertTask(googleTask)
                saveReminder(reminder)
                withContext(UI) {
                    isInProgress.postValue(false)
                    result.postValue(Commands.SAVED)
                }
            } catch (e: IOException) {
                withContext(UI) {
                    isInProgress.postValue(false)
                    result.postValue(Commands.FAILED)
                }
            }
        }
    }

    fun updateGoogleTask(googleTask: GoogleTask, reminder: Reminder?) {
        val google = Google.getInstance()
        if (google?.tasks == null) {
            return
        }
        val isConnected = SuperUtil.isConnected(getApplication())
        if (!isConnected) {
            result.postValue(Commands.FAILED)
            return
        }
        isInProgress.postValue(true)
        launch(CommonPool) {
            appDb.googleTasksDao().insert(googleTask)
            try {
                google.tasks?.updateTask(googleTask)
                saveReminder(reminder)
                withContext(UI) {
                    isInProgress.postValue(false)
                    result.postValue(Commands.SAVED)
                }
            } catch (e: IOException) {
                withContext(UI) {
                    isInProgress.postValue(false)
                    result.postValue(Commands.FAILED)
                }
            }
        }
    }

    fun updateAndMoveGoogleTask(googleTask: GoogleTask, oldListId: String, reminder: Reminder?) {
        val google = Google.getInstance()
        if (google?.tasks == null) {
            return
        }
        val isConnected = SuperUtil.isConnected(getApplication())
        if (!isConnected) {
            result.postValue(Commands.FAILED)
            return
        }
        isInProgress.postValue(true)
        launch(CommonPool) {
            appDb.googleTasksDao().insert(googleTask)
            try {
                google.tasks?.updateTask(googleTask)
                google.tasks?.moveTask(googleTask, oldListId)
                saveReminder(reminder)
                withContext(UI) {
                    isInProgress.postValue(false)
                    result.postValue(Commands.SAVED)
                }
            } catch (e: IOException) {
                withContext(UI) {
                    isInProgress.postValue(false)
                    result.postValue(Commands.FAILED)
                }
            }
        }
    }

    fun moveGoogleTask(googleTask: GoogleTask, oldListId: String) {
        val google = Google.getInstance()
        if (google?.tasks == null) {
            return
        }
        val isConnected = SuperUtil.isConnected(getApplication())
        if (!isConnected) {
            result.postValue(Commands.FAILED)
            return
        }
        isInProgress.postValue(true)
        launch(CommonPool) {
            appDb.googleTasksDao().insert(googleTask)
            google.tasks?.moveTask(googleTask, oldListId)
            withContext(UI) {
                isInProgress.postValue(false)
                result.postValue(Commands.SAVED)
            }
        }
    }

    class Factory(private val application: Application, private val id: String) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GoogleTaskViewModel(application, id) as T
        }
    }
}
