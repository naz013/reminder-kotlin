package com.elementary.tasks.module.logicschedule

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.logic.schedule.SchedulePreferences
import com.github.naz013.logic.schedule.WorkerNetworkType

class SchedulePreferencesImpl(
  private val prefs: Prefs
) : SchedulePreferences {

  override val workerNetworkType: WorkerNetworkType
    get() = prefs.workerNetworkType
}
