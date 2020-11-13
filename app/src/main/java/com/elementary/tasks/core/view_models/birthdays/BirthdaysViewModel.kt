package com.elementary.tasks.core.view_models.birthdays

import com.elementary.tasks.birthdays.work.DeleteBackupWorker
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.Commands
import kotlinx.coroutines.runBlocking

class BirthdaysViewModel : BaseBirthdaysViewModel() {

  val birthdays = appDb.birthdaysDao().loadAll()

  fun deleteAllBirthdays() {
    postInProgress(true)
    launchDefault {
      runBlocking {
        val list = appDb.birthdaysDao().all()
        for (birthday in list) {
          appDb.birthdaysDao().delete(birthday)
          startWork(DeleteBackupWorker::class.java, Constants.INTENT_ID, birthday.uuId)
        }
      }
      updateBirthdayPermanent()
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }
}
