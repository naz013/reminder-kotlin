package com.elementary.tasks.core.view_models.groups

import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.groups.work.SingleBackupWorker

class GroupsViewModel : BaseGroupsViewModel() {

    fun changeGroupColor(reminderGroup: ReminderGroup, color: Int) {
        postInProgress(true)
        launchDefault {
            reminderGroup.groupColor = color
            appDb.reminderGroupDao().insert(reminderGroup)
            startWork(SingleBackupWorker::class.java, Constants.INTENT_ID, reminderGroup.groupUuId)
            withUIContext { postInProgress(false) }
        }
    }
}
