package com.elementary.tasks.reminder.create.fragments.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.adapter.UiUsedTimeListAdapter
import com.elementary.tasks.core.data.observeTable
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.UsedTime
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.repository.UsedTimeRepository
import com.github.naz013.repository.observer.TableChangeListenerFactory
import com.github.naz013.repository.table.Table
import kotlinx.coroutines.launch

class UsedTimeViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val usedTimeRepository: UsedTimeRepository,
  private val uiUsedTimeListAdapter: UiUsedTimeListAdapter,
  tableChangeListenerFactory: TableChangeListenerFactory
) : ViewModel() {

  val usedTimeList = viewModelScope.observeTable(
    table = Table.UsedTime,
    tableChangeListenerFactory = tableChangeListenerFactory,
    queryProducer = { usedTimeRepository.getFirst(limit = 5) }
  ).map { list ->
    list.map { uiUsedTimeListAdapter.convert(it) }
  }

  fun saveTime(after: Long) {
    viewModelScope.launch(dispatcherProvider.default()) {
      var old = usedTimeRepository.getByTimeMills(after)
      old = old?.copy(
        useCount = old.useCount + 1
      ) ?: UsedTime(0, DateTimeManager.generateViewAfterString(after), after, 1)
      usedTimeRepository.save(old)
    }
  }
}
