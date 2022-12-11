package com.elementary.tasks.core.view_models.used_time

import com.elementary.tasks.core.data.dao.UsedTimeDao
import com.elementary.tasks.core.data.models.UsedTime
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.WorkManagerProvider
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.core.view_models.DispatcherProvider

class UsedTimeViewModel(
  prefs: Prefs,
  dispatcherProvider: DispatcherProvider,
  workManagerProvider: WorkManagerProvider,
  private val usedTimeDao: UsedTimeDao
) : BaseDbViewModel(prefs, dispatcherProvider, workManagerProvider) {

  val usedTimeList = usedTimeDao.loadFirst5()

  fun saveTime(after: Long) {
    launchDefault {
      var old = usedTimeDao.getByTimeMills(after)
      if (old != null) {
        old.useCount = old.useCount + 1
      } else {
        old = UsedTime(0, TimeUtil.generateViewAfterString(after), after, 1)
      }
      usedTimeDao.insert(old)
    }
  }
}