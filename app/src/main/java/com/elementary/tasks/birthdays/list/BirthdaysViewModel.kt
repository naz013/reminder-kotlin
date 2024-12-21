package com.elementary.tasks.birthdays.list

import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.birthdays.work.BirthdayDeleteBackupWorker
import com.elementary.tasks.core.appwidgets.UpdatesHelper
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayListAdapter
import com.elementary.tasks.core.data.livedata.SearchableLiveData
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.github.naz013.domain.Birthday
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.repository.BirthdayRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class BirthdaysViewModel(
  private val birthdayRepository: BirthdayRepository,
  private val uiBirthdayListAdapter: UiBirthdayListAdapter,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val notifier: Notifier,
  private val updatesHelper: UpdatesHelper
) : BaseProgressViewModel(dispatcherProvider) {

  private val birthdaysData = SearchableBirthdayData(
    dispatcherProvider = dispatcherProvider,
    parentScope = viewModelScope,
    birthdayRepository = birthdayRepository
  )
  val birthdays = birthdaysData.map { list ->
    list.map { uiBirthdayListAdapter.convert(it) }.sortedBy { it.nextBirthdayDateMillis }
  }

  fun onSearchUpdate(query: String) {
    birthdaysData.onNewQuery(query)
  }

  fun deleteBirthday(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      birthdayRepository.delete(id)
      notifier.showBirthdayPermanent()
      workerLauncher.startWork(BirthdayDeleteBackupWorker::class.java, Constants.INTENT_ID, id)
      birthdaysData.refresh()
      updatesHelper.updateTasksWidget()
      updatesHelper.updateBirthdaysWidget()
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  internal class SearchableBirthdayData(
    dispatcherProvider: DispatcherProvider,
    parentScope: CoroutineScope,
    private val birthdayRepository: BirthdayRepository
  ) : SearchableLiveData<List<Birthday>>(parentScope + dispatcherProvider.default()) {

    override suspend fun runQuery(query: String): List<Birthday> {
      return if (query.isEmpty()) {
        birthdayRepository.getAll()
      } else {
        birthdayRepository.searchByName(query.lowercase())
      }
    }
  }
}
