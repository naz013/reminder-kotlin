package com.elementary.tasks.core.view_models.google_tasks

import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.view_models.Commands
import kotlinx.coroutines.runBlocking

class GoogleTaskListsViewModel : BaseTaskListsViewModel() {

    val googleTaskLists = appDb.googleTaskListsDao().loadAll()
    val defaultTaskList = appDb.googleTaskListsDao().loadDefault()

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
}
