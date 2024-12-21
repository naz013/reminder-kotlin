package com.elementary.tasks.voice

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.adapter.UiReminderListAdapter
import com.elementary.tasks.core.data.observeTable
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.mapNullable
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.repository.observer.TableChangeListenerFactory
import com.github.naz013.repository.table.Table

class VoiceResultDialogViewModel(
  id: String,
  private val reminderRepository: ReminderRepository,
  private val uiReminderListAdapter: UiReminderListAdapter,
  dispatcherProvider: DispatcherProvider,
  tableChangeListenerFactory: TableChangeListenerFactory
) : BaseProgressViewModel(dispatcherProvider) {

  val reminder = viewModelScope.observeTable(
    table = Table.Reminder,
    tableChangeListenerFactory = tableChangeListenerFactory,
    queryProducer = { reminderRepository.getById(id) }
  ).mapNullable {
    uiReminderListAdapter.create(it)
  }
  var hasSameInDb: Boolean = false
}
