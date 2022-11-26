package com.elementary.tasks.core.view_models.groups

import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.groups.work.GroupDeleteBackupWorker

abstract class BaseGroupsViewModel(
  appDb: AppDb,
  prefs: Prefs,
  dispatcherProvider: DispatcherProvider
) : BaseDbViewModel(appDb, prefs, dispatcherProvider) {

  val allGroups = appDb.reminderGroupDao().loadAll()

  fun deleteGroup(reminderGroup: ReminderGroup) {
    postInProgress(true)
    launchDefault {
      appDb.reminderGroupDao().delete(reminderGroup)
      postInProgress(false)
      postCommand(Commands.DELETED)
      startWork(GroupDeleteBackupWorker::class.java, Constants.INTENT_ID, reminderGroup.groupUuId)
    }
  }
}
