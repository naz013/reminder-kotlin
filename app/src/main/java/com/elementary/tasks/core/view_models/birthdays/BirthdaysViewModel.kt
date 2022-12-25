package com.elementary.tasks.core.view_models.birthdays

import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.birthdays.list.BirthdayModelAdapter
import com.elementary.tasks.birthdays.work.BirthdayDeleteBackupWorker
import com.elementary.tasks.core.data.dao.BirthdaysDao
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import kotlinx.coroutines.launch

class BirthdaysViewModel(
  birthdaysDao: BirthdaysDao,
  private val birthdayModelAdapter: BirthdayModelAdapter,
  dispatcherProvider: DispatcherProvider,
  workerLauncher: WorkerLauncher,
  notifier: Notifier
) : BaseBirthdaysViewModel(birthdaysDao, dispatcherProvider, workerLauncher, notifier) {

  val birthdays = birthdaysDao.loadAll().map { list ->
    list.map { birthdayModelAdapter.convert(it) }
  }

  fun deleteAllBirthdays() {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val list = birthdaysDao.all()
      for (birthday in list) {
        birthdaysDao.delete(birthday)
        workerLauncher.startWork(
          BirthdayDeleteBackupWorker::class.java,
          Constants.INTENT_ID,
          birthday.uuId
        )
      }
      updateBirthdayPermanent()
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }
}
