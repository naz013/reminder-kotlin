package com.elementary.tasks.core.view_models.birthdays

import android.content.Context
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.birthdays.list.BirthdayModelAdapter
import com.elementary.tasks.birthdays.work.BirthdayDeleteBackupWorker
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import kotlinx.coroutines.launch

class BirthdaysViewModel(
  appDb: AppDb,
  prefs: Prefs,
  context: Context,
  private val birthdayModelAdapter: BirthdayModelAdapter,
  dispatcherProvider: DispatcherProvider
) : BaseBirthdaysViewModel(appDb, prefs, context, dispatcherProvider) {

  val birthdays = appDb.birthdaysDao().loadAll().map { list ->
    list.map { birthdayModelAdapter.convert(it) }
  }

  fun deleteAllBirthdays() {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
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
