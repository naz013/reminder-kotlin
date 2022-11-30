package com.elementary.tasks.core.controller

import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import timber.log.Timber

class TimerEvent(
  reminder: Reminder,
  appDb: AppDb,
  prefs: Prefs,
  calendarUtils: CalendarUtils,
  notifier: Notifier,
  jobScheduler: JobScheduler,
  updatesHelper: UpdatesHelper,
  textProvider: TextProvider
) : RepeatableEventManager(
  reminder,
  appDb,
  prefs,
  calendarUtils,
  notifier,
  jobScheduler,
  updatesHelper,
  textProvider
) {

  override val isActive: Boolean
    get() = reminder.isActive

  override fun start(): Boolean {
    if (TimeCount.isCurrent(reminder.eventTime)) {
      reminder.isActive = true
      reminder.isRemoved = false
      super.save()
      super.enableReminder()
      super.export()
    }
    return true
  }

  override fun skip(): Boolean {
    reminder.delay = 0
    if (canSkip()) {
      val time = calculateTime(false)
      reminder.eventTime = TimeUtil.getGmtFromDateTime(time)
      start()
      return true
    }
    return false
  }

  override fun next(): Boolean {
    reminder.delay = 0
    return if (canSkip()) {
      var time = calculateTime(false)
      while (time < System.currentTimeMillis()) {
        reminder.eventTime = TimeUtil.getGmtFromDateTime(time)
        time = calculateTime(false)
      }
      Timber.d("next: ${TimeUtil.getFullDateTime(time, true)}")
      reminder.eventTime = TimeUtil.getGmtFromDateTime(time)
      reminder.eventCount = reminder.eventCount + 1
      start()
    } else {
      stop()
    }
  }

  override fun onOff(): Boolean {
    return if (isActive) {
      stop()
    } else {
      var time = calculateTime(true)
      while (time < System.currentTimeMillis()) {
        reminder.eventTime = TimeUtil.getGmtFromDateTime(time)
        time = calculateTime(true)
      }
      reminder.eventTime = TimeUtil.getGmtFromDateTime(time)
      reminder.eventCount = 0
      start()
    }
  }

  override fun canSkip(): Boolean {
    return reminder.isRepeating() && (!reminder.isLimited() || !reminder.isLimitExceed())
  }

  override fun setDelay(delay: Int) {
    if (delay == 0) {
      next()
      return
    }
    reminder.delay = delay
    super.save()
    super.setDelay(delay)
  }

  override fun calculateTime(isNew: Boolean): Long {
    return TimeCount.generateNextTimer(reminder, isNew)
  }
}
