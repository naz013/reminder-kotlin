package com.elementary.tasks.reminder.lists.todo

import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.Commands
import com.github.naz013.feature.common.livedata.SearchableLiveData
import com.github.naz013.common.intent.IntentKeys
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.reminder.lists.data.UiReminderListsAdapter
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import com.github.naz013.domain.Reminder
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.repository.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class ActiveTodoRemindersViewModel(
  dispatcherProvider: DispatcherProvider,
  private val reminderRepository: ReminderRepository,
  private val eventControlFactory: EventControlFactory,
  private val workerLauncher: WorkerLauncher,
  private val uiReminderListsAdapter: UiReminderListsAdapter
) : BaseProgressViewModel(dispatcherProvider) {

  private val reminderData = SearchableTodoReminderData(
    dispatcherProvider = dispatcherProvider,
    parentScope = viewModelScope,
    reminderRepository = reminderRepository
  )
  val events = reminderData.map { uiReminderListsAdapter.convert(it) }

  fun onSearchUpdate(query: String) {
    reminderData.onNewQuery(query)
  }

  fun skip(id: String) {
    withResultSuspend {
      val fromDb = reminderRepository.getById(id)
      if (fromDb != null) {
        eventControlFactory.getController(fromDb).skip()
        workerLauncher.startWork(
          ReminderSingleBackupWorker::class.java,
          IntentKeys.INTENT_ID,
          fromDb.uuId
        )
        reminderData.refresh()
        Commands.SAVED
      }
      Commands.FAILED
    }
  }

  fun toggleReminder(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val item = reminderRepository.getById(id) ?: return@launch
      if (!eventControlFactory.getController(item).onOff()) {
        postInProgress(false)
        postCommand(Commands.OUTDATED)
      } else {
        workerLauncher.startWork(
          ReminderSingleBackupWorker::class.java,
          IntentKeys.INTENT_ID,
          item.uuId
        )
        postInProgress(false)
        postCommand(Commands.SAVED)
      }
      reminderData.refresh()
    }
  }

  fun moveToTrash(id: String) {
    withResultSuspend {
      reminderRepository.getById(id)?.let {
        it.isRemoved = true
        eventControlFactory.getController(it).disable()
        reminderRepository.save(it)
        workerLauncher.startWork(
          ReminderSingleBackupWorker::class.java,
          IntentKeys.INTENT_ID,
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
    private val reminderRepository: ReminderRepository
  ) : SearchableLiveData<List<Reminder>>(parentScope + dispatcherProvider.default()) {

    override suspend fun runQuery(query: String): List<Reminder> {
      return if (query.isEmpty()) {
        reminderRepository.getAllTypes(
          removed = false,
          active = true,
          types = TYPES
        )
      } else {
        reminderRepository.searchBySummaryAllTypes(
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
