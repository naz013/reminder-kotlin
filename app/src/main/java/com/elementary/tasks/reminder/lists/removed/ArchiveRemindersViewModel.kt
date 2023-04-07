package com.elementary.tasks.reminder.lists.removed

import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.UiReminderListAdapter
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.livedata.SearchableLiveData
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.ui.UiReminderListData
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.reminder.work.ReminderDeleteBackupWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class ArchiveRemindersViewModel(
  private val reminderDao: ReminderDao,
  private val googleCalendarUtils: GoogleCalendarUtils,
  private val eventControlFactory: EventControlFactory,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val uiReminderListAdapter: UiReminderListAdapter
) : BaseProgressViewModel(dispatcherProvider) {

  private val reminderData = SearchableReminderData(dispatcherProvider, viewModelScope, reminderDao)
  val events = reminderData.map { list ->
    list.map { uiReminderListAdapter.create(it) }
  }

  fun onSearchUpdate(query: String) {
    reminderData.onNewQuery(query)
  }

  fun hasEvents(): Boolean {
    return events.value?.isNotEmpty() ?: false
  }

  fun deleteReminder(reminder: UiReminderListData) {
    withResult {
      reminderDao.getById(reminder.id)?.let {
        eventControlFactory.getController(it).stop()
        reminderDao.delete(it)
        googleCalendarUtils.deleteEvents(it.uuId)
        workerLauncher.startWork(
          ReminderDeleteBackupWorker::class.java,
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

  fun deleteAll() {
    val reminders = reminderData.value ?: return
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      reminders.forEach {
        eventControlFactory.getController(it).stop()
      }
      reminderDao.deleteAll(reminders)
      reminders.forEach {
        workerLauncher.startWork(
          ReminderDeleteBackupWorker::class.java,
          Constants.INTENT_ID,
          it.uuId
        )
      }
      reminderData.refresh()
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  internal class SearchableReminderData(
    dispatcherProvider: DispatcherProvider,
    parentScope: CoroutineScope,
    private val reminderDao: ReminderDao
  ) : SearchableLiveData<List<Reminder>>(parentScope + dispatcherProvider.default()) {

    override fun runQuery(query: String): List<Reminder> {
      return if (query.isEmpty()) {
        reminderDao.getByRemovedStatus(removed = true)
      } else {
        reminderDao.searchBySummaryAndRemovedStatus(query.lowercase(), removed = true)
      }
    }
  }
}
