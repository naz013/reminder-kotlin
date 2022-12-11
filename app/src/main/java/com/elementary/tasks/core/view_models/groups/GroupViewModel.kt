package com.elementary.tasks.core.view_models.groups

import com.elementary.tasks.core.data.dao.ReminderGroupDao
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.WorkManagerProvider
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.groups.work.GroupSingleBackupWorker

class GroupViewModel(
  id: String,
  prefs: Prefs,
  dispatcherProvider: DispatcherProvider,
  workManagerProvider: WorkManagerProvider,
  reminderGroupDao: ReminderGroupDao
) : BaseGroupsViewModel(prefs, dispatcherProvider, workManagerProvider, reminderGroupDao) {

  val reminderGroup = reminderGroupDao.loadById(id)
  var isEdited = false
  var hasSameInDb: Boolean = false
  var isFromFile: Boolean = false

  fun findSame(id: String) {
    launchDefault {
      val group = reminderGroupDao.getById(id)
      hasSameInDb = group != null
    }
  }

  fun saveGroup(reminderGroup: ReminderGroup, wasDefault: Boolean) {
    postInProgress(true)
    launchDefault {
      if (!wasDefault && reminderGroup.isDefaultGroup) {
        val groups = reminderGroupDao.all()
        for (g in groups) g.isDefaultGroup = false
        reminderGroupDao.insertAll(groups)
      }
      reminderGroupDao.insert(reminderGroup)
      startWork(GroupSingleBackupWorker::class.java, Constants.INTENT_ID, reminderGroup.groupUuId)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }
}
