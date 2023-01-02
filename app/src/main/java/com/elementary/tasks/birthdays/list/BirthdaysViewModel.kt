package com.elementary.tasks.birthdays.list

import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.birthdays.work.BirthdayDeleteBackupWorker
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayListAdapter
import com.elementary.tasks.core.data.dao.BirthdaysDao
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.view_models.BaseProgressViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import kotlinx.coroutines.launch

class BirthdaysViewModel(
  private val birthdaysDao: BirthdaysDao,
  private val uiBirthdayListAdapter: UiBirthdayListAdapter,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val notifier: Notifier
) : BaseProgressViewModel(dispatcherProvider) {

  val birthdays = birthdaysDao.loadAll().map { list ->
    list.map { uiBirthdayListAdapter.convert(it) }
  }

  fun deleteBirthday(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      birthdaysDao.delete(id)
      notifier.showBirthdayPermanent()
      workerLauncher.startWork(BirthdayDeleteBackupWorker::class.java, Constants.INTENT_ID, id)
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }
}
