package com.elementary.tasks.reminder.lists.active

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.observeTable
import com.elementary.tasks.core.utils.DispatcherProvider
import com.github.naz013.domain.Reminder
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.repository.observer.TableChangeListenerFactory
import com.github.naz013.repository.table.Table

class ActiveGpsRemindersViewModel(
  reminderRepository: ReminderRepository,
  dispatcherProvider: DispatcherProvider,
  tableChangeListenerFactory: TableChangeListenerFactory
) : BaseProgressViewModel(dispatcherProvider) {

  val events = viewModelScope.observeTable(
    table = Table.Reminder,
    tableChangeListenerFactory = tableChangeListenerFactory,
    queryProducer = {
      reminderRepository.getAllTypes(
        active = true,
        removed = false,
        types = Reminder.gpsTypes()
      )
    }
  )
}
