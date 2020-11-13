package com.elementary.tasks.core.services

import android.app.AlarmManager
import android.content.Context
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.elementary.tasks.birthdays.work.CheckBirthdaysWorker
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

object EventJobScheduler {

  const val EVENT_BIRTHDAY = "event_birthday"
  const val EVENT_BIRTHDAY_PERMANENT = "event_birthday_permanent"
  const val EVENT_AUTO_SYNC = "event_auto_sync"
  const val EVENT_AUTO_BACKUP = "event_auto_backup"
  const val EVENT_CHECK = "event_check"
  private const val EVENT_CHECK_BIRTHDAYS = "event_check_birthday"

  const val ARG_LOCATION = "arg_location"
  const val ARG_MISSED = "arg_missed"
  const val ARG_REPEAT = "arg_repeated"

  fun scheduleEventCheck(prefs: Prefs) {
    val interval = prefs.autoCheckInterval
    if (interval <= 0) {
      cancelEventCheck()
      return
    }
    val millis = AlarmManager.INTERVAL_HOUR * interval
    JobRequest.Builder(EVENT_CHECK)
      .setExact(millis)
      .setRequiresCharging(false)
      .setRequiresDeviceIdle(false)
      .setRequiresBatteryNotLow(false)
      .build()
      .schedule()
  }

  fun cancelEventCheck() {
    cancelReminder(EVENT_CHECK)
  }

  fun scheduleBirthdaysCheck(context: Context) {
    val work = PeriodicWorkRequest.Builder(CheckBirthdaysWorker::class.java, 24, TimeUnit.HOURS, 1, TimeUnit.HOURS)
      .addTag(EVENT_CHECK_BIRTHDAYS)
      .build()
    WorkManager.getInstance(context).enqueue(work)
  }

  fun cancelBirthdaysCheck(context: Context) {
    WorkManager.getInstance(context).cancelAllWorkByTag(EVENT_CHECK_BIRTHDAYS)
  }

  fun scheduleBirthdayPermanent() {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = System.currentTimeMillis()
    val currTime = calendar.timeInMillis
    calendar.set(Calendar.HOUR_OF_DAY, 5)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    var millis = calendar.timeInMillis
    while (currTime > millis) {
      calendar.add(Calendar.DAY_OF_MONTH, 1)
      millis = calendar.timeInMillis
    }
    JobRequest.Builder(EVENT_BIRTHDAY_PERMANENT)
      .setExact(millis)
      .setRequiresCharging(false)
      .setRequiresDeviceIdle(false)
      .setRequiresBatteryNotLow(false)
      .build()
      .schedule()
  }

  fun cancelBirthdayPermanent() {
    cancelReminder(EVENT_BIRTHDAY_PERMANENT)
  }

  fun scheduleAutoSync(prefs: Prefs) {
    val interval = prefs.autoSyncState
    if (interval <= 0) {
      cancelAutoSync()
      return
    }
    val millis = AlarmManager.INTERVAL_HOUR * interval
    JobRequest.Builder(EVENT_AUTO_SYNC)
      .setExact(millis)
      .setRequiresCharging(false)
      .setRequiresDeviceIdle(false)
      .setRequiresBatteryNotLow(false)
      .build()
      .schedule()
  }

  private fun cancelAutoSync() {
    cancelReminder(EVENT_AUTO_SYNC)
  }

  fun scheduleAutoBackup(prefs: Prefs) {
    val interval = prefs.autoBackupState
    if (interval <= 0) {
      cancelAutoBackup()
      return
    }
    val millis = AlarmManager.INTERVAL_HOUR * interval
    JobRequest.Builder(EVENT_AUTO_BACKUP)
      .setExact(millis)
      .setRequiresCharging(false)
      .setRequiresDeviceIdle(false)
      .setRequiresBatteryNotLow(false)
      .build()
      .schedule()
  }

  private fun cancelAutoBackup() {
    cancelReminder(EVENT_AUTO_BACKUP)
  }

  fun cancelDailyBirthday() {
    cancelReminder(EVENT_BIRTHDAY)
  }

  fun scheduleDailyBirthday(prefs: Prefs) {
    val time = prefs.birthdayTime
    val millis = TimeUtil.getBirthdayTime(time) - System.currentTimeMillis()
    if (millis <= 0) return
    JobRequest.Builder(EVENT_BIRTHDAY)
      .setExact(millis)
      .setRequiresCharging(false)
      .setRequiresDeviceIdle(false)
      .setRequiresBatteryNotLow(false)
      .build()
      .schedule()
  }

  fun scheduleMissedCall(prefs: Prefs, number: String?) {
    if (number == null) return
    val time = prefs.missedReminderTime
    val millis = (time * (1000 * 60)).toLong()
    val bundle = PersistableBundleCompat()
    bundle.putBoolean(ARG_MISSED, true)
    JobRequest.Builder(number)
      .setExact(millis)
      .setRequiresCharging(false)
      .setRequiresDeviceIdle(false)
      .setRequiresBatteryNotLow(false)
      .setRequiresStorageNotLow(false)
      .setExtras(bundle)
      .setUpdateCurrent(true)
      .build()
      .schedule()
  }

  fun cancelMissedCall(number: String?) {
    if (number == null) return
    cancelReminder(number)
  }

  fun scheduleReminderRepeat(context: Context, uuId: String, prefs: Prefs): Boolean {
    val item = AppDb.getAppDatabase(context).reminderDao().getById(uuId) ?: return false
    val minutes = prefs.notificationRepeatTime
    val millis = minutes * TimeCount.MINUTE
    if (millis <= 0) {
      return false
    }
    Timber.d("scheduleReminderRepeat: $millis, $uuId")
    val bundle = PersistableBundleCompat()
    bundle.putBoolean(ARG_REPEAT, true)
    JobRequest.Builder(item.uuId)
      .setExact(millis)
      .setRequiresCharging(false)
      .setRequiresDeviceIdle(false)
      .setRequiresBatteryNotLow(false)
      .setRequiresStorageNotLow(false)
      .setUpdateCurrent(true)
      .setExtras(bundle)
      .build()
      .schedule()
    return true
  }

  fun scheduleReminderDelay(minutes: Int, uuId: String) {
    scheduleReminderDelay(TimeCount.MINUTE * minutes, uuId)
  }

  fun scheduleReminderDelay(millis: Long, uuId: String) {
    if (millis <= 0) {
      return
    }
    Timber.d("scheduleReminderDelay: $millis, $uuId")
    JobRequest.Builder(uuId)
      .setExact(millis)
      .setRequiresCharging(false)
      .setRequiresDeviceIdle(false)
      .setRequiresBatteryNotLow(false)
      .setRequiresStorageNotLow(false)
      .setUpdateCurrent(true)
      .build()
      .schedule()
  }

  fun scheduleGpsDelay(context: Context, uuId: String): Boolean {
    val item = AppDb.getAppDatabase(context).reminderDao().getById(uuId) ?: return false
    val due = TimeUtil.getDateTimeFromGmt(item.eventTime)
    val millis = due - System.currentTimeMillis()
    if (due == 0L || millis <= 0) {
      return false
    }
    Timber.d("scheduleGpsDelay: $millis, $uuId")
    val bundle = PersistableBundleCompat()
    bundle.putBoolean(ARG_LOCATION, true)
    JobRequest.Builder(item.uuId)
      .setExact(millis)
      .setRequiresCharging(false)
      .setRequiresDeviceIdle(false)
      .setRequiresBatteryNotLow(false)
      .setRequiresStorageNotLow(false)
      .setUpdateCurrent(true)
      .setExtras(bundle)
      .build()
      .schedule()
    return true
  }

  fun scheduleReminder(reminder: Reminder?) {
    if (reminder == null) return
    var due = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
    Timber.d("scheduleReminder: ${TimeUtil.logTime(due)}")
    Timber.d("scheduleReminder: noe -> ${TimeUtil.logTime()}")
    if (due == 0L) {
      return
    }
    if (reminder.remindBefore != 0L) {
      due -= reminder.remindBefore
    }
    if (!Reminder.isBase(reminder.type, Reminder.BY_TIME)) {
      val calendar = Calendar.getInstance()
      calendar.timeInMillis = due
      calendar.set(Calendar.SECOND, 0)
      calendar.set(Calendar.MILLISECOND, 0)
      due = calendar.timeInMillis
    }
    var millis = due - System.currentTimeMillis()
    if (millis < 0) {
      millis = 100L
    }
    JobRequest.Builder(reminder.uuId)
      .setExact(millis)
      .setRequiresCharging(false)
      .setRequiresDeviceIdle(false)
      .setRequiresBatteryNotLow(false)
      .setRequiresStorageNotLow(false)
      .setUpdateCurrent(true)
      .build()
      .schedule()
  }

  fun isEventScheduled(uuId: String): Boolean {
    return JobManager.instance().getAllJobsForTag(uuId).isNotEmpty()
  }

  fun cancelReminder(uuId: String) {
    Timber.i("cancelReminder: $uuId")
    JobManager.instance().cancelAllForTag(uuId)
  }
}