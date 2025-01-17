package com.elementary.tasks.core.controller

import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.datetime.plusMillis
import com.github.naz013.domain.Reminder
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter
import org.threeten.bp.LocalDateTime

class YearlyEvent(
  reminder: Reminder,
  reminderRepository: ReminderRepository,
  prefs: Prefs,
  googleCalendarUtils: GoogleCalendarUtils,
  notifier: Notifier,
  jobScheduler: JobScheduler,
  appWidgetUpdater: AppWidgetUpdater,
  textProvider: TextProvider,
  private val dateTimeManager: DateTimeManager,
  googleTaskRepository: GoogleTaskRepository,
  private val modelDateTimeFormatter: ModelDateTimeFormatter
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
    if (canSkip()) {
      val time = modelDateTimeFormatter.getNextYearDayTime(
        reminder,
        dateTimeManager.fromGmtToLocal(reminder.eventTime)?.plusMillis(1000L)
          ?: LocalDateTime.now()
      )
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
        val time = modelDateTimeFormatter.getNextYearDayTime(
          reminder,
          dateTimeManager.fromGmtToLocal(reminder.eventTime)?.plusMillis(1000L)
            ?: LocalDateTime.now()
        )
        reminder.eventTime = dateTimeManager.getGmtFromDateTime(time)
      }
      reminder.eventCount = 0
      enable()
    }
  }

  override fun canSkip(): Boolean {
    return !reminder.isLimited() || !reminder.isLimitExceed()
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
    return modelDateTimeFormatter.getNextYearDayTime(reminder)
  }
}
