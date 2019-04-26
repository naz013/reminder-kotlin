package com.elementary.tasks.core.view_models.groups

import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.groups.work.DeleteBackupWorker

abstract class BaseGroupsViewModel : BaseDbViewModel() {

    val allGroups = appDb.reminderGroupDao().loadAll()

    fun deleteGroup(reminderGroup: ReminderGroup) {
        postInProgress(true)
        launchDefault {
            appDb.reminderGroupDao().delete(reminderGroup)
            postInProgress(false)
            postCommand(Commands.DELETED)
            startWork(DeleteBackupWorker::class.java, Constants.INTENT_ID, reminderGroup.groupUuId)
        }
    }
}
