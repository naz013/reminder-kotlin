package com.elementary.tasks.birthdays.list

import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.birthdays.work.BirthdayDeleteBackupWorker
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayListAdapter
import com.elementary.tasks.core.data.dao.BirthdaysDao
import com.elementary.tasks.core.data.livedata.SearchableLiveData
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.work.WorkerLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class BirthdaysViewModel(
  private val birthdaysDao: BirthdaysDao,
  private val uiBirthdayListAdapter: UiBirthdayListAdapter,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val notifier: Notifier
) : BaseProgressViewModel(dispatcherProvider) {

  private val birthdaysData = SearchableBirthdayData(
    dispatcherProvider,
    viewModelScope,
    birthdaysDao
  )
  val birthdays = birthdaysData.map { list ->
    list.map { uiBirthdayListAdapter.convert(it) }.sortedBy { it.nextBirthdayDate }
  }

  fun onSearchUpdate(query: String) {
    birthdaysData.onNewQuery(query)
  }

  fun deleteBirthday(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      birthdaysDao.delete(id)
      notifier.showBirthdayPermanent()
      workerLauncher.startWork(BirthdayDeleteBackupWorker::class.java, Constants.INTENT_ID, id)
      birthdaysData.refresh()
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  internal class SearchableBirthdayData(
    dispatcherProvider: DispatcherProvider,
    parentScope: CoroutineScope,
    private val birthdaysDao: BirthdaysDao
  ) : SearchableLiveData<List<Birthday>>(parentScope + dispatcherProvider.default()) {

    override fun runQuery(query: String): List<Birthday> {
      return if (query.isEmpty()) {
        birthdaysDao.getAll()
      } else {
        birthdaysDao.searchByName(query.lowercase())
      }
    }
  }
}
