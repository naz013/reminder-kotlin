package com.elementary.tasks.reminder.dialog

import androidx.lifecycle.viewModelScope
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.observeTable
import com.github.naz013.common.intent.IntentKeys
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import com.github.naz013.domain.Reminder
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.repository.observer.TableChangeListenerFactory
import com.github.naz013.repository.table.Table
import kotlinx.coroutines.launch

class ReminderViewModel(
  id: String,
  private val reminderRepository: ReminderRepository,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val appWidgetUpdater: AppWidgetUpdater,
  tableChangeListenerFactory: TableChangeListenerFactory
) : BaseProgressViewModel(dispatcherProvider) {

  val reminder = viewModelScope.observeTable(
    table = Table.Reminder,
    tableChangeListenerFactory = tableChangeListenerFactory,
    queryProducer = { reminderRepository.getById(id) }
  )

  fun saveReminder(reminder: Reminder) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderRepository.save(reminder)
      appWidgetUpdater.updateScheduleWidget()
      workerLauncher.startWork(
        ReminderSingleBackupWorker::class.java,
        IntentKeys.INTENT_ID,
        reminder.uuId
      )
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }
}
