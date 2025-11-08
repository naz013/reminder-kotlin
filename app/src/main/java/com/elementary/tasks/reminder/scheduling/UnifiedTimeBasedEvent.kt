package com.elementary.tasks.reminder.scheduling

import com.elementary.tasks.core.controller.RepeatableEventManager
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
import org.threeten.bp.LocalDateTime

/**
 * Unified EventControl implementation for all time-based reminders.
 * Uses injected ReminderBehaviorStrategy to determine specific behavior.
 *
 * This controller replaces: DateEvent, WeeklyEvent, MonthlyEvent, YearlyEvent,
 * TimerEvent, and RecurEvent by using the Strategy pattern to handle different
 * repeat patterns without relying on the reminderType property.
 */
class UnifiedTimeBasedEvent(
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
  private val behaviorStrategy: ReminderBehaviorStrategy
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
      return true
    }
    return false
  }

  override fun skip(): Boolean {
    if (canSkip()) {
      val currentTime = dateTimeManager.fromGmtToLocal(reminder.eventTime)
        ?.plusMillis(1000) ?: LocalDateTime.now()
      val nextTime = behaviorStrategy.calculateNextOccurrence(reminder, currentTime)

      if (nextTime != null) {
        reminder.eventTime = dateTimeManager.getGmtFromDateTime(nextTime)
        enable()
        return true
      }
    }
    return false
  }

  override fun next(): Boolean {
    reminder.delay = 0
    return if (canSkip()) {
      val time = calculateTime(false)
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
      if (!dateTimeManager.isCurrent(reminder.eventTime)) {
        val currentTime = dateTimeManager.fromGmtToLocal(reminder.eventTime)
          ?.plusMillis(1000) ?: LocalDateTime.now()
        val nextTime = behaviorStrategy.calculateNextOccurrence(reminder, currentTime)

        if (nextTime != null) {
          reminder.eventTime = dateTimeManager.getGmtFromDateTime(nextTime)
          reminder.startTime = dateTimeManager.getGmtFromDateTime(nextTime)
        }
      }
      reminder.eventCount = 0
      enable()
    }
  }

  override fun canSkip(): Boolean {
    return behaviorStrategy.canSkip(reminder)
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
    val fromTime = if (isNew) {
      LocalDateTime.now()
    } else {
      dateTimeManager.fromGmtToLocal(reminder.eventTime) ?: LocalDateTime.now()
    }

    return behaviorStrategy.calculateNextOccurrence(reminder, fromTime)
      ?: fromTime // Fallback to current time if no next occurrence
  }
}

