package com.elementary.tasks.core.controller

import android.text.TextUtils
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.Notifier
import com.github.naz013.common.datetime.DateTimeManager
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.domain.Reminder
import com.github.naz013.common.TextProvider
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.ReminderRepository
import org.threeten.bp.LocalDateTime

class ShoppingEvent(
  reminder: Reminder,
  reminderRepository: ReminderRepository,
  prefs: Prefs,
  googleCalendarUtils: GoogleCalendarUtils,
  notifier: Notifier,
  jobScheduler: JobScheduler,
  appWidgetUpdater: AppWidgetUpdater,
  textProvider: TextProvider,
  private val dateTimeManager: DateTimeManager,
  googleTaskRepository: GoogleTaskRepository
) : RepeatableEventManager(
  reminder,
  reminderRepository,
  prefs,
  googleCalendarUtils,
  notifier,
  jobScheduler,
  appWidgetUpdater,
  textProvider,
  dateTimeManager,
  googleTaskRepository
) {

  override val isActive: Boolean
    get() = reminder.isActive

  override fun justStart() {
    if (
      reminder.hasReminder && !TextUtils.isEmpty(reminder.eventTime) &&
      dateTimeManager.isCurrent(reminder.eventTime)
    ) {
      reminder.isActive = true
      reminder.isRemoved = false
      super.save()
      super.enableReminder()
    } else {
      reminder.isActive = true
      reminder.isRemoved = false
      super.save()
    }
  }

  override fun enable(): Boolean {
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
      enable()
      return true
    }
    return false
  }

  override fun next(): Boolean {
    return disable()
  }

  override fun onOff(): Boolean {
    return if (isActive) {
      disable()
    } else {
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
    return dateTimeManager.generateDateTime(reminder.eventTime, reminder.repeatInterval)
  }
}
