package com.elementary.tasks.core.view_models.groups

import com.elementary.tasks.core.data.AppDb
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
  appDb: AppDb,
  prefs: Prefs,
  dispatcherProvider: DispatcherProvider,
  workManagerProvider: WorkManagerProvider
) : BaseGroupsViewModel(appDb, prefs, dispatcherProvider, workManagerProvider) {

  val reminderGroup = appDb.reminderGroupDao().loadById(id)
  var isEdited = false
  var hasSameInDb: Boolean = false
  var isFromFile: Boolean = false
  var isLogged = false

  fun findSame(id: String) {
    launchDefault {
      val group = appDb.reminderGroupDao().getById(id)
      hasSameInDb = group != null
    }
  }

  fun saveGroup(reminderGroup: ReminderGroup, wasDefault: Boolean) {
    postInProgress(true)
    launchDefault {
      if (!wasDefault && reminderGroup.isDefaultGroup) {
        val groups = appDb.reminderGroupDao().all()
        for (g in groups) g.isDefaultGroup = false
        appDb.reminderGroupDao().insertAll(groups)
      }
      appDb.reminderGroupDao().insert(reminderGroup)
      startWork(GroupSingleBackupWorker::class.java, Constants.INTENT_ID, reminderGroup.groupUuId)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }
}
