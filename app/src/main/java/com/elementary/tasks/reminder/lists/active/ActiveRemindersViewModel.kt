package com.elementary.tasks.reminder.lists.active

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.UiReminderListsAdapter
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.ui.UiReminderList
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class ActiveRemindersViewModel(
  dispatcherProvider: DispatcherProvider,
  private val reminderDao: ReminderDao,
  private val eventControlFactory: EventControlFactory,
  private val workerLauncher: WorkerLauncher,
  private val uiReminderListsAdapter: UiReminderListsAdapter
) : BaseProgressViewModel(dispatcherProvider) {

  private val reminderData = SearchableReminderData(dispatcherProvider, viewModelScope, reminderDao)
  val events = Transformations.map(reminderData) {
    uiReminderListsAdapter.convert(it)
  }

  fun onSearchUpdate(query: String) {
    reminderData.onNewQuery(query)
  }

  fun skip(reminder: UiReminderList) {
    withResult {
      val fromDb = reminderDao.getById(reminder.id)
      if (fromDb != null) {
        eventControlFactory.getController(fromDb).skip()
        workerLauncher.startWork(ReminderSingleBackupWorker::class.java, Constants.INTENT_ID, fromDb.uuId)
        reminderData.refresh()
        Commands.SAVED
      }
      Commands.FAILED
    }
  }

  fun toggleReminder(reminder: UiReminderList) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val item = reminderDao.getById(reminder.id) ?: return@launch
      if (!eventControlFactory.getController(item).onOff()) {
        postInProgress(false)
        postCommand(Commands.OUTDATED)
      } else {
        workerLauncher.startWork(ReminderSingleBackupWorker::class.java, Constants.INTENT_ID, item.uuId)
        postInProgress(false)
        postCommand(Commands.SAVED)
      }
      reminderData.refresh()
    }
  }

  fun moveToTrash(reminder: UiReminderList) {
    withResult {
      reminderDao.getById(reminder.id)?.let {
        it.isRemoved = true
        eventControlFactory.getController(it).stop()
        reminderDao.insert(it)
        workerLauncher.startWork(ReminderSingleBackupWorker::class.java, Constants.INTENT_ID, it.uuId)
        reminderData.refresh()
        Commands.DELETED
      } ?: run {
        Commands.FAILED
      }
    }
  }

  internal class SearchableReminderData(
    dispatcherProvider: DispatcherProvider,
    parentScope: CoroutineScope,
    private val reminderDao: ReminderDao
  ) : LiveData<List<Reminder>>() {

    private val scope = parentScope + dispatcherProvider.default()
    private var job: Job? = null
    private var query: String = ""

    fun refresh() {
      load()
    }

    fun onNewQuery(s: String) {
      if (query != s) {
        query = s
        load()
      }
    }

    override fun onActive() {
      super.onActive()
      load()
    }

    override fun onInactive() {
      super.onInactive()
      job?.cancel()
    }

    private fun load() {
      job?.cancel()
      job = scope.launch {
        val result = if (query.isEmpty()) {
          reminderDao.getByRemovedStatus(removed = false)
        } else {
          reminderDao.searchBySummaryAndRemovedStatus(query, removed = false)
        }
        postValue(result)
      }
    }
  }
}
