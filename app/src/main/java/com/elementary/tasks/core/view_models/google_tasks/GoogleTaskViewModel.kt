package com.elementary.tasks.core.view_models.google_tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.view_models.Commands
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
class GoogleTaskViewModel(id: String) : BaseTaskListsViewModel() {

    var googleTask: LiveData<GoogleTask>
    var defaultTaskList: LiveData<GoogleTaskList>
    var defaultReminderGroup: LiveData<ReminderGroup>
    var googleTaskLists: LiveData<List<GoogleTaskList>>

    private var _reminder = MutableLiveData<Reminder>()
    var reminder: LiveData<Reminder> = _reminder

    init {
        googleTask = appDb.googleTasksDao().loadById(id)
        defaultTaskList = appDb.googleTaskListsDao().loadDefault()
        googleTaskLists = appDb.googleTaskListsDao().loadAll()
        defaultReminderGroup = appDb.reminderGroupDao().loadDefault()
    }

    fun loadReminder(uuId: String) {
        postInProgress(true)
        launchDefault {
            val reminderItem = appDb.reminderDao().getById(uuId)
            withUIContext {
                _reminder.postValue(reminderItem)
                postInProgress(false)
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
        val google = GTasks.getInstance(context)
        if (google == null) {
            postCommand(Commands.FAILED)
            return
        }
        postInProgress(true)
        launchDefault {
            try {
                google.deleteTask(googleTask)
                appDb.googleTasksDao().delete(googleTask)
                withUIContext {
                    postInProgress(false)
                    postCommand(Commands.DELETED)
                }
            } catch (e: IOException) {
                withUIContext {
                    postInProgress(false)
                    postCommand(Commands.FAILED)
                }
            }
        }
    }

    fun newGoogleTask(googleTask: GoogleTask, reminder: Reminder?) {
        val google = GTasks.getInstance(context)
        if (google == null) {
            postCommand(Commands.FAILED)
            return
        }
        postInProgress(true)
        launchDefault {
            try {
                google.insertTask(googleTask)
                saveReminder(reminder)
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

    fun updateGoogleTask(googleTask: GoogleTask, reminder: Reminder?) {
        val google = GTasks.getInstance(context)
        if (google == null) {
            postCommand(Commands.FAILED)
            return
        }
        postInProgress(true)
        launchDefault {
            appDb.googleTasksDao().insert(googleTask)
            try {
                google.updateTask(googleTask)
                saveReminder(reminder)
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

    fun updateAndMoveGoogleTask(googleTask: GoogleTask, oldListId: String, reminder: Reminder?) {
        val google = GTasks.getInstance(context)
        if (google == null) {
            postCommand(Commands.FAILED)
            return
        }
        postInProgress(true)
        launchDefault {
            appDb.googleTasksDao().insert(googleTask)
            try {
                google.updateTask(googleTask)
                google.moveTask(googleTask, oldListId)
                saveReminder(reminder)
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

    fun moveGoogleTask(googleTask: GoogleTask, oldListId: String) {
        val google = GTasks.getInstance(context)
        if (google == null) {
            postCommand(Commands.FAILED)
            return
        }
        postInProgress(true)
        launchDefault {
            appDb.googleTasksDao().insert(googleTask)
            google.moveTask(googleTask, oldListId)
            withUIContext {
                postInProgress(false)
                postCommand(Commands.SAVED)
            }
        }
    }

    class Factory(private val id: String) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GoogleTaskViewModel(id) as T
        }
    }
}
