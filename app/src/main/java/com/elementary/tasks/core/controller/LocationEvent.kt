package com.elementary.tasks.core.controller

import android.content.Context
import android.text.TextUtils
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.feature.common.coroutine.invokeSuspend
import com.elementary.tasks.core.services.GeolocationService
import com.elementary.tasks.core.services.JobScheduler
import com.github.naz013.common.Module
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.SuperUtil
import com.github.naz013.common.datetime.DateTimeManager
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.domain.Reminder
import com.github.naz013.repository.ReminderRepository
import org.threeten.bp.LocalDateTime

class LocationEvent(
  reminder: Reminder,
  private val reminderRepository: ReminderRepository,
  prefs: Prefs,
  private val context: Context,
  notifier: Notifier,
  private val jobScheduler: JobScheduler,
  appWidgetUpdater: AppWidgetUpdater,
  private val dateTimeManager: DateTimeManager
) : EventManager(reminder, reminderRepository, prefs, notifier, appWidgetUpdater) {

  override val isActive: Boolean
    get() = reminder.isActive

  override fun justStart() {
    if (Module.hasLocation(context)) {
      reminder.isActive = true
      reminder.isRemoved = false
      super.save()
      if (!jobScheduler.scheduleGpsDelay(reminder)) {
        SuperUtil.startGpsTracking(context)
      }
    }
  }

  override fun enable(): Boolean {
    return if (Module.hasLocation(context)) {
      reminder.isActive = true
      reminder.isRemoved = false
      super.save()
      if (jobScheduler.scheduleGpsDelay(reminder)) {
        true
      } else {
        SuperUtil.startGpsTracking(context)
        true
      }
    } else {
      disable()
      remove()
      false
    }
  }

  override fun disable(): Boolean {
    jobScheduler.cancelReminder(reminder.uniqueId)
    reminder.isActive = false
    if (prefs.moveCompleted) {
      reminder.isRemoved = true
    }
    super.save()
    notifier.cancel(reminder.uniqueId)
    stopTracking(false)
    return true
  }

  private fun stopTracking(isPaused: Boolean) {
    val list = invokeSuspend {
      reminderRepository.getAllTypes(
        active = true,
        removed = false,
        types = Reminder.gpsTypes()
      )
    }
    if (list.isEmpty()) {
      SuperUtil.stopService(context, GeolocationService::class.java)
    }
    var hasActive = false
    for (item in list) {
      if (isPaused) {
        if (item.uniqueId == reminder.uniqueId) {
          continue
        }
        if (TextUtils.isEmpty(item.eventTime) || !dateTimeManager.isCurrent(item.eventTime)) {
          if (!item.isNotificationShown) {
            hasActive = true
            break
          }
        } else {
          if (!item.isNotificationShown) {
            hasActive = true
            break
          }
        }
      } else {
        if (!item.isNotificationShown) {
          hasActive = true
          break
        }
      }
    }
    if (!hasActive) {
      SuperUtil.stopService(context, GeolocationService::class.java)
    }
  }

  override fun pause(): Boolean {
    jobScheduler.cancelReminder(reminder.uniqueId)
    notifier.cancel(reminder.uniqueId)
    stopTracking(true)
    return true
  }

  override fun skip(): Boolean {
    return false
  }

  override fun resume(): Boolean {
    if (reminder.isActive) {
      val b = jobScheduler.scheduleGpsDelay(reminder)
      if (!b) SuperUtil.startGpsTracking(context)
    }
    return true
  }

  override fun next(): Boolean {
    return disable()
  }

  override fun onOff(): Boolean {
    return if (isActive) {
      disable()
    } else {
      reminder.isLocked = false
      reminder.isNotificationShown = false
      super.save()
      enable()
    }
  }

  override fun canSkip(): Boolean {
    return false
  }

  override fun setDelay(delay: Int) {
  }

  override fun calculateTime(isNew: Boolean): LocalDateTime {
    return dateTimeManager.generateDateTime(reminder.eventTime, reminder.repeatInterval)
  }
}
