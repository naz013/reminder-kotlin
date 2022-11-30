package com.elementary.tasks.core.controller

import android.content.Context
import android.text.TextUtils
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.GeolocationService
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TimeCount

class LocationEvent(
  reminder: Reminder,
  appDb: AppDb,
  prefs: Prefs,
  private val context: Context,
  notifier: Notifier,
  jobScheduler: JobScheduler,
  updatesHelper: UpdatesHelper
) : EventManager(reminder, appDb, prefs, notifier, jobScheduler, updatesHelper) {

  override val isActive: Boolean
    get() = reminder.isActive

  override fun start(): Boolean {
    return if (Module.hasLocation(context)) {
      reminder.isActive = true
      reminder.isRemoved = false
      super.save()
      if (jobScheduler.scheduleGpsDelay(db, reminder.uuId)) {
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
    jobScheduler.cancelReminder(reminder.uuId)
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
    val list = db.reminderDao().getAllTypes(
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
        if (TextUtils.isEmpty(item.eventTime) || !TimeCount.isCurrent(item.eventTime)) {
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
    jobScheduler.cancelReminder(reminder.uuId)
    notifier.cancel(reminder.uniqueId)
    stopTracking(true)
    return true
  }

  override fun skip(): Boolean {
    return false
  }

  override fun resume(): Boolean {
    if (reminder.isActive) {
      val b = jobScheduler.scheduleGpsDelay(db, reminder.uuId)
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

  override fun calculateTime(isNew: Boolean): Long {
    return TimeCount.generateDateTime(reminder.eventTime, reminder.repeatInterval)
  }
}
