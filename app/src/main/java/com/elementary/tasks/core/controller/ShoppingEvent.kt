package com.elementary.tasks.core.controller

import android.text.TextUtils
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

class ShoppingEvent(
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
    return if (reminder.hasReminder) {
      if (!TextUtils.isEmpty(reminder.eventTime) && TimeCount.isCurrent(reminder.eventTime)) {
        reminder.isActive = true
        reminder.isRemoved = false
        super.save()
        super.enableReminder()
        true
      } else {
        false
      }
    } else {
      reminder.isActive = true
      reminder.isRemoved = false
      super.save()
      true
    }
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
    return stop()
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