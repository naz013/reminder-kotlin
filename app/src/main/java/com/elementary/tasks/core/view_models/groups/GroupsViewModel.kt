package com.elementary.tasks.core.view_models.groups

import com.elementary.tasks.core.data.dao.ReminderGroupDao
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.WorkManagerProvider
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.groups.work.GroupSingleBackupWorker

class GroupsViewModel(
  prefs: Prefs,
  dispatcherProvider: DispatcherProvider,
  workManagerProvider: WorkManagerProvider,
  reminderGroupDao: ReminderGroupDao
) : BaseGroupsViewModel(prefs, dispatcherProvider, workManagerProvider, reminderGroupDao) {

  fun changeGroupColor(reminderGroup: ReminderGroup, color: Int) {
    postInProgress(true)
    launchDefault {
      reminderGroup.groupColor = color
      reminderGroupDao.insert(reminderGroup)
      startWork(GroupSingleBackupWorker::class.java, Constants.INTENT_ID, reminderGroup.groupUuId)
      withUIContext { postInProgress(false) }
    }
  }
}
