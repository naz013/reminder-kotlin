package com.elementary.tasks.core.view_models.google_tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.Commands
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class GoogleTaskViewModel(id: String) : BaseTaskListsViewModel() {

    val googleTask = appDb.googleTasksDao().loadById(id)
    val defaultTaskList = appDb.googleTaskListsDao().loadDefault()
    val googleTaskLists = appDb.googleTaskListsDao().loadAll()

    private var _reminder = MutableLiveData<Reminder>()
    var reminder: LiveData<Reminder> = _reminder

    fun loadReminder(uuId: String) {
        postInProgress(true)
        launchDefault {
            val reminderItem = appDb.reminderDao().getById(uuId)
            _reminder.postValue(reminderItem)
            postInProgress(false)
        }
    }

    private fun saveReminder(reminder: Reminder?) {
        Timber.d("saveReminder: $reminder")
        if (reminder != null) {
            launchDefault {
                runBlocking {
                    val group = appDb.reminderGroupDao().defaultGroup()
                    if (group != null) {
                        reminder.groupColor = group.groupColor
                        reminder.groupTitle = group.groupTitle
                        reminder.groupUuId = group.groupUuId
                        appDb.reminderDao().insert(reminder)
                    }
                }
                if (reminder.groupUuId != "") {
                    EventControlFactory.getController(reminder).start()
                    startWork(com.elementary.tasks.reminder.work.SingleBackupWorker::class.java,
                            Constants.INTENT_ID, reminder.uuId)
                }
            }
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
                postInProgress(false)
                postCommand(Commands.DELETED)
            } catch (e: Exception) {
                postInProgress(false)
                postCommand(Commands.FAILED)
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
                postInProgress(false)
                postCommand(Commands.SAVED)
            } catch (e: Exception) {
                postInProgress(false)
                postCommand(Commands.FAILED)
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
                postInProgress(false)
                postCommand(Commands.SAVED)
            } catch (e: Exception) {
                postInProgress(false)
                postCommand(Commands.FAILED)
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
                postInProgress(false)
                postCommand(Commands.SAVED)
            } catch (e: Exception) {
                postInProgress(false)
                postCommand(Commands.FAILED)
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
            postInProgress(false)
            postCommand(Commands.SAVED)
        }
    }

    class Factory(private val id: String) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GoogleTaskViewModel(id) as T
        }
    }
}
