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
import com.elementary.tasks.core.utils.datetime.RecurEventManager
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.logging.Logger
import org.threeten.bp.LocalDateTime

class RecurEvent(
  reminder: Reminder,
  reminderDao: ReminderDao,
  prefs: Prefs,
  googleCalendarUtils: GoogleCalendarUtils,
  notifier: Notifier,
  jobScheduler: JobScheduler,
  updatesHelper: UpdatesHelper,
  textProvider: TextProvider,
  private val dateTimeManager: DateTimeManager,
  googleTasksDao: GoogleTasksDao,
  private val recurEventManager: RecurEventManager
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
    Logger.d("start: ${reminder.eventTime}")
    if (dateTimeManager.isCurrent(reminder.eventTime)) {
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
      val time = calculateTime(false)
      reminder.eventTime = dateTimeManager.getGmtFromDateTime(time)
      reminder.eventCount = reminder.eventCount + 1
      enable()
    } else {
      disable()
    }
  }

  override fun onOff(): Boolean {
    return if (isActive) {
      disable()
    } else {
      if (!dateTimeManager.isCurrent(reminder.eventTime)) {
        if (canSkip()) {
          val time = calculateTime(false)
          reminder.eventTime = dateTimeManager.getGmtFromDateTime(time)
          reminder.startTime = dateTimeManager.getGmtFromDateTime(time)
          reminder.eventCount = 0
          enable()
        } else {
          disable()
        }
      } else {
        reminder.eventCount = 0
        enable()
      }
    }
  }

  override fun canSkip(): Boolean {
    return getNextDateTime() != null
  }

  private fun getNextDateTime(): LocalDateTime? {
    val currentEventTime = dateTimeManager.fromGmtToLocal(reminder.eventTime)
    return recurEventManager.getNextAfterDateTime(currentEventTime, reminder.recurDataObject)
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
    return getNextDateTime()!!
  }
}
