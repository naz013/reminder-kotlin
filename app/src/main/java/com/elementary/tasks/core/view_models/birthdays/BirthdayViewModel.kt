package com.elementary.tasks.core.view_models.birthdays

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.birthdays.work.SingleBackupWorker
import com.elementary.tasks.core.data.dao.BirthdaysDao
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.view_models.BaseProgressViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import kotlinx.coroutines.launch

class BirthdayViewModel(
  id: String,
  private val birthdaysDao: BirthdaysDao,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val notifier: Notifier
) : BaseProgressViewModel(dispatcherProvider) {

  val birthday = birthdaysDao.loadById(id)

  fun saveBirthday(birthday: Birthday) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      birthday.updatedAt = DateTimeManager.gmtDateTime
      birthdaysDao.insert(birthday)
      notifier.showBirthdayPermanent()
      workerLauncher.startWork(SingleBackupWorker::class.java, Constants.INTENT_ID, birthday.uuId)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }
}
