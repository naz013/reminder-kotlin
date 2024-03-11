package com.elementary.tasks.reminder.lists.todo

import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.UiReminderListsAdapter
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.livedata.SearchableLiveData
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.ui.UiReminderList
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class ActiveTodoRemindersViewModel(
  dispatcherProvider: DispatcherProvider,
  private val reminderDao: ReminderDao,
  private val eventControlFactory: EventControlFactory,
  private val workerLauncher: WorkerLauncher,
  private val uiReminderListsAdapter: UiReminderListsAdapter
) : BaseProgressViewModel(dispatcherProvider) {

  private val reminderData = SearchableTodoReminderData(
    dispatcherProvider = dispatcherProvider,
    parentScope = viewModelScope,
    reminderDao = reminderDao
  )
  val events = reminderData.map { uiReminderListsAdapter.convert(it) }

  fun onSearchUpdate(query: String) {
    reminderData.onNewQuery(query)
  }

  fun skip(reminder: UiReminderList) {
    withResult {
      val fromDb = reminderDao.getById(reminder.id)
      if (fromDb != null) {
        eventControlFactory.getController(fromDb).skip()
        workerLauncher.startWork(
          ReminderSingleBackupWorker::class.java,
          Constants.INTENT_ID,
          fromDb.uuId
        )
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
        workerLauncher.startWork(
          ReminderSingleBackupWorker::class.java,
          Constants.INTENT_ID,
          item.uuId
        )
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
        eventControlFactory.getController(it).disable()
        reminderDao.insert(it)
        workerLauncher.startWork(
          ReminderSingleBackupWorker::class.java,
          Constants.INTENT_ID,
          it.uuId
        )
        reminderData.refresh()
        Commands.DELETED
      } ?: run {
        Commands.FAILED
      }
    }
  }

  internal class SearchableTodoReminderData(
    dispatcherProvider: DispatcherProvider,
    parentScope: CoroutineScope,
    private val reminderDao: ReminderDao
  ) : SearchableLiveData<List<Reminder>>(parentScope + dispatcherProvider.default()) {

    override fun runQuery(query: String): List<Reminder> {
      return if (query.isEmpty()) {
        reminderDao.getAllTypes(
          removed = false,
          active = true,
          types = TYPES
        )
      } else {
        reminderDao.searchBySummaryAllTypes(
          query = query.lowercase(),
          removed = false,
          active = true,
          types = TYPES
        )
      }
    }
  }

  companion object {
    private val TYPES = intArrayOf(Reminder.BY_DATE_SHOP)
  }
}
