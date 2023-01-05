package com.elementary.tasks.reminder.create.fragments

import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.adapter.UiUsedTimeListAdapter
import com.elementary.tasks.core.data.dao.UsedTimeDao
import com.elementary.tasks.core.data.models.UsedTime
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import kotlinx.coroutines.launch

class UsedTimeViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val usedTimeDao: UsedTimeDao,
  private val uiUsedTimeListAdapter: UiUsedTimeListAdapter
) : ViewModel() {

  val usedTimeList = Transformations.map(usedTimeDao.loadFirst5()) { list ->
    list.map { uiUsedTimeListAdapter.convert(it) }
  }

  fun saveTime(after: Long) {
    viewModelScope.launch(dispatcherProvider.default()) {
      var old = usedTimeDao.getByTimeMills(after)
      old = old?.copy(
        useCount = old.useCount + 1
      ) ?: UsedTime(0, DateTimeManager.generateViewAfterString(after), after, 1)
      usedTimeDao.insert(old)
    }
  }
}