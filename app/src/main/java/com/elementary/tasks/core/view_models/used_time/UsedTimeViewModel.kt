package com.elementary.tasks.core.view_models.used_time

import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.UsedTime
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.core.view_models.DispatcherProvider

class UsedTimeViewModel(
  appDb: AppDb,
  prefs: Prefs,
  dispatcherProvider: DispatcherProvider
) : BaseDbViewModel(appDb, prefs, dispatcherProvider) {

  val usedTimeList = appDb.usedTimeDao().loadFirst5()

  fun saveTime(after: Long) {
    launchDefault {
      var old = appDb.usedTimeDao().getByTimeMills(after)
      if (old != null) {
        old.useCount = old.useCount + 1
      } else {
        old = UsedTime(0, TimeUtil.generateViewAfterString(after), after, 1)
      }
      appDb.usedTimeDao().insert(old)
    }
  }
}