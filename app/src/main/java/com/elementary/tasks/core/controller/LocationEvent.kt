package com.elementary.tasks.core.controller

import android.content.Context
import android.text.TextUtils
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.GeolocationService
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.params.Prefs
import org.threeten.bp.LocalDateTime

class LocationEvent(
  reminder: Reminder,
  private val reminderDao: ReminderDao,
  prefs: Prefs,
  private val context: Context,
  notifier: Notifier,
  private val jobScheduler: JobScheduler,
  updatesHelper: UpdatesHelper,
  private val dateTimeManager: DateTimeManager
) : EventManager(reminder, reminderDao, prefs, notifier, updatesHelper) {

  override val isActive: Boolean
    get() = reminder.isActive

  override fun start(): Boolean {
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
      stop()
      remove()
      false
    }
  }

  override fun stop(): Boolean {
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
    val list = reminderDao.getAllTypes(
      active = true,
      removed = false,
      types = Reminder.gpsTypes()
    )
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
    return stop()
  }

  override fun onOff(): Boolean {
    return if (isActive) {
      stop()
    } else {
      reminder.isLocked = false
      reminder.isNotificationShown = false
      super.save()
      start()
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
