package com.elementary.tasks.core.view_models.birthdays

import android.content.Context
import com.elementary.tasks.birthdays.work.BirthdayDeleteBackupWorker
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.Commands

class BirthdaysViewModel(
  appDb: AppDb,
  prefs: Prefs,
  context: Context
) : BaseBirthdaysViewModel(appDb, prefs, context) {

  val birthdays = appDb.birthdaysDao().loadAll()

  fun deleteAllBirthdays() {
    postInProgress(true)
    launchDefault {
      val list = appDb.birthdaysDao().all()
      for (birthday in list) {
        appDb.birthdaysDao().delete(birthday)
        startWork(BirthdayDeleteBackupWorker::class.java, Constants.INTENT_ID, birthday.uuId)
      }
      updateBirthdayPermanent()
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }
}
