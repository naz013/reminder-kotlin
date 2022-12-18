package com.elementary.tasks.core.controller

import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.datetime.TimeCount
import com.elementary.tasks.core.utils.datetime.TimeUtil

class DateEvent(
  reminder: Reminder,
  appDb: AppDb,
  prefs: Prefs,
  googleCalendarUtils: GoogleCalendarUtils,
  notifier: Notifier,
  jobScheduler: JobScheduler,
  updatesHelper: UpdatesHelper,
  textProvider: TextProvider
) : RepeatableEventManager(
  reminder,
  appDb,
  prefs,
  googleCalendarUtils,
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
      return true
    }
    return false
  }

  override fun skip(): Boolean {
    if (canSkip()) {
      val time = TimeCount.generateDateTime(
        reminder.eventTime,
        reminder.repeatInterval,
        TimeUtil.getDateTimeFromGmt(reminder.eventTime)
      )
      reminder.eventTime = TimeUtil.getGmtFromDateTime(time)
      start()
      return true
    }
    return false
  }

  override fun next(): Boolean {
    reminder.delay = 0
    return if (canSkip()) {
      val time = calculateTime(false)
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
    return TimeCount.generateDateTime(reminder.eventTime, reminder.repeatInterval)
  }
}