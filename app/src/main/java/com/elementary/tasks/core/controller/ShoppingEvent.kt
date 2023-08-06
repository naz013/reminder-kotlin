package com.elementary.tasks.core.controller

import android.text.TextUtils
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
import org.threeten.bp.LocalDateTime

class ShoppingEvent(
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

  override fun start(): Boolean {
    return if (reminder.hasReminder) {
      if (!TextUtils.isEmpty(reminder.eventTime) && dateTimeManager.isCurrent(reminder.eventTime)) {
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
      val time = dateTimeManager.generateDateTime(
        reminder.eventTime,
        reminder.repeatInterval,
        dateTimeManager.fromGmtToLocal(reminder.eventTime) ?: LocalDateTime.now()
      )
      reminder.eventTime = dateTimeManager.getGmtFromDateTime(time)
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

  override fun calculateTime(isNew: Boolean): LocalDateTime {
    return dateTimeManager.generateDateTime(reminder.eventTime, reminder.repeatInterval)
  }
}
