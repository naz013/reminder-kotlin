package com.elementary.tasks.reminder.dialog

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.observeTable
import com.elementary.tasks.reminder.usecase.SaveReminderUseCase
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.repository.observer.TableChangeListenerFactory
import com.github.naz013.repository.table.Table
import kotlinx.coroutines.launch

class ReminderViewModel(
  id: String,
  private val reminderRepository: ReminderRepository,
  dispatcherProvider: DispatcherProvider,
  tableChangeListenerFactory: TableChangeListenerFactory,
  private val saveReminderUseCase: SaveReminderUseCase
) : BaseProgressViewModel(dispatcherProvider) {

  val reminder = viewModelScope.observeTable(
    table = Table.Reminder,
    tableChangeListenerFactory = tableChangeListenerFactory,
    queryProducer = { reminderRepository.getById(id) }
  )

  fun saveReminder(reminder: Reminder) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      saveReminderUseCase(
        reminder.copy(
          version = reminder.version + 1,
          syncState = SyncState.WaitingForUpload
        )
      )
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }
}
