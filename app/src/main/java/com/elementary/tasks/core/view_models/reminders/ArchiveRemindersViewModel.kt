package com.elementary.tasks.core.view_models.reminders

import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.reminder.work.DeleteBackupWorker
import kotlinx.coroutines.runBlocking

class ArchiveRemindersViewModel : BaseRemindersViewModel() {

  val events = appDb.reminderDao().loadNotRemoved(true)

  fun deleteAll(data: List<Reminder>) {
    postInProgress(true)
    launchDefault {
      runBlocking {
        data.forEach {
          EventControlFactory.getController(it).stop()
        }
        appDb.reminderDao().deleteAll(data)
      }
      data.forEach {
        startWork(DeleteBackupWorker::class.java, Constants.INTENT_ID, it.uuId)
      }
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }
}
