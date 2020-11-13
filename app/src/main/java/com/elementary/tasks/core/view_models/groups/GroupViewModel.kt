package com.elementary.tasks.core.view_models.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.groups.work.SingleBackupWorker
import kotlinx.coroutines.runBlocking

class GroupViewModel private constructor(id: String) : BaseGroupsViewModel() {

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
      runBlocking {
        if (!wasDefault && reminderGroup.isDefaultGroup) {
          val groups = appDb.reminderGroupDao().all()
          for (g in groups) g.isDefaultGroup = false
          appDb.reminderGroupDao().insertAll(groups)
        }
        appDb.reminderGroupDao().insert(reminderGroup)
      }
      startWork(SingleBackupWorker::class.java, Constants.INTENT_ID, reminderGroup.groupUuId)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }

  class Factory(private val id: String) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return GroupViewModel(id) as T
    }
  }
}
