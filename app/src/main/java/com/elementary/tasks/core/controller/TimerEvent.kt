package com.elementary.tasks.core.controller

import com.elementary.tasks.core.appwidgets.UpdatesHelper
import com.elementary.tasks.core.data.dao.GoogleTasksDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.logging.Logger
import org.threeten.bp.LocalDateTime

class TimerEvent(
  reminder: Reminder,
  reminderDao: ReminderDao,
  prefs: Prefs,
  googleCalendarUtils: GoogleCalendarUtils,
  notifier: Notifier,
  jobScheduler: JobScheduler,
  updatesHelper: UpdatesHelper,
  textProvider: TextProvider,
  private val dateTimeManager: DateTimeManager,
  googleTasksDao: GoogleTasksDao
) : RepeatableEventManager(
  reminder,
  reminderDao,
  prefs,
  googleCalendarUtils,
  notifier,
  jobScheduler,
  updatesHelper,
  textProvider,
  dateTimeManager,
  googleTasksDao
) {

  override val isActive: Boolean
    get() = reminder.isActive

  override fun justStart() {
    reminder.isActive = true
    reminder.isRemoved = false
    super.save()
    super.enableReminder()
    super.export()
  }

  override fun enable(): Boolean {
    if (dateTimeManager.isCurrent(reminder.eventTime)) {
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
      reminder.eventTime = dateTimeManager.getGmtFromDateTime(time)
      enable()
      return true
    }
    return false
  }

  override fun next(): Boolean {
    reminder.delay = 0
    return if (canSkip()) {
      var time = calculateTime(false)
      while (!dateTimeManager.isCurrent(time)) {
        reminder.eventTime = dateTimeManager.getGmtFromDateTime(time)
        time = calculateTime(false)
      }
      Logger.d("next: ${dateTimeManager.logDateTime(time)}")
      reminder.eventTime = dateTimeManager.getGmtFromDateTime(time)
      reminder.eventCount += 1
      enable()
    } else {
      disable()
    }
  }

  override fun onOff(): Boolean {
    return if (isActive) {
      disable()
    } else {
      var time = calculateTime(true)
      while (!dateTimeManager.isCurrent(time)) {
        reminder.eventTime = dateTimeManager.getGmtFromDateTime(time)
        time = calculateTime(true)
      }
      reminder.eventTime = dateTimeManager.getGmtFromDateTime(time)
      reminder.eventCount = 0
      enable()
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

  override fun calculateTime(isNew: Boolean): LocalDateTime {
    return dateTimeManager.generateNextTimer(reminder, isNew)
  }
}
