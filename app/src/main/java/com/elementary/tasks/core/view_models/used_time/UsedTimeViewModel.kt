package com.elementary.tasks.core.view_models.used_time

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.dao.UsedTimeDao
import com.elementary.tasks.core.data.models.UsedTime
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.view_models.DispatcherProvider
import kotlinx.coroutines.launch

class UsedTimeViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val usedTimeDao: UsedTimeDao
) : ViewModel() {

  val usedTimeList = usedTimeDao.loadFirst5()

  fun saveTime(after: Long) {
    viewModelScope.launch(dispatcherProvider.default()) {
      var old = usedTimeDao.getByTimeMills(after)
      if (old != null) {
        old.useCount = old.useCount + 1
      } else {
        old = UsedTime(0, DateTimeManager.generateViewAfterString(after), after, 1)
      }
      usedTimeDao.insert(old)
    }
  }
}