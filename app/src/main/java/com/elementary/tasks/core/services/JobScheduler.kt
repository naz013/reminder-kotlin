package com.elementary.tasks.core.services

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.elementary.tasks.birthdays.work.CheckBirthdaysWorker
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.minusMillis
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.google_tasks.work.SaveNewTaskWorker
import com.elementary.tasks.google_tasks.work.UpdateTaskWorker
import com.google.gson.Gson
import timber.log.Timber
import java.util.Calendar
import java.util.concurrent.TimeUnit

class JobScheduler(
  private val context: Context,
  private val prefs: Prefs,
  private val dateTimeManager: DateTimeManager
) {

  fun scheduleEventCheck() {
    val interval = prefs.autoCheckInterval
    if (interval <= 0) {
      cancelEventCheck()
      return
    }
    val millis = INTERVAL_HOUR * interval

    val work = OneTimeWorkRequest.Builder(EventJobService::class.java)
      .setInitialDelay(millis, TimeUnit.MILLISECONDS)
      .addTag(EVENT_CHECK)
      .setConstraints(getDefaultConstraints())
      .build()
    schedule(work)
  }

  fun cancelEventCheck() {
    cancelReminder(EVENT_CHECK)
  }

  fun scheduleBirthdaysCheck() {
    val work = PeriodicWorkRequest.Builder(
      CheckBirthdaysWorker::class.java,
      24,
      TimeUnit.HOURS,
      1,
      TimeUnit.HOURS
    )
      .addTag(EVENT_CHECK_BIRTHDAYS)
      .build()
    schedule(work)
  }

  fun cancelBirthdaysCheck() {
    cancelReminder(EVENT_CHECK_BIRTHDAYS)
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

    val work = OneTimeWorkRequest.Builder(EventJobService::class.java)
      .setInitialDelay(millis - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
      .addTag(EVENT_BIRTHDAY_PERMANENT)
      .setConstraints(getDefaultConstraints())
      .build()

    schedule(work)
  }

  fun cancelBirthdayPermanent() {
    cancelReminder(EVENT_BIRTHDAY_PERMANENT)
  }

  fun scheduleAutoSync() {
    val interval = prefs.autoSyncState
    if (interval <= 0) {
      cancelAutoSync()
      return
    }

    val millis = INTERVAL_HOUR * interval

    val work = OneTimeWorkRequest.Builder(EventJobService::class.java)
      .setInitialDelay(millis, TimeUnit.MILLISECONDS)
      .addTag(EVENT_AUTO_SYNC)
      .setConstraints(getDefaultConstraints())
      .build()

    schedule(work)
  }

  private fun cancelAutoSync() {
    cancelReminder(EVENT_AUTO_SYNC)
  }

  fun scheduleAutoBackup() {
    val interval = prefs.autoBackupState
    if (interval <= 0) {
      cancelAutoBackup()
      return
    }
    val millis = INTERVAL_HOUR * interval

    val work = OneTimeWorkRequest.Builder(EventJobService::class.java)
      .setInitialDelay(millis, TimeUnit.MILLISECONDS)
      .addTag(EVENT_AUTO_BACKUP)
      .setConstraints(getDefaultConstraints())
      .build()

    schedule(work)
  }

  private fun cancelAutoBackup() {
    cancelReminder(EVENT_AUTO_BACKUP)
  }

  fun cancelDailyBirthday() {
    cancelReminder(EVENT_BIRTHDAY)
  }

  fun scheduleDailyBirthday() {
    val millis = dateTimeManager.getMillisToBirthdayTime()
    if (millis <= 0) return

    val work = OneTimeWorkRequest.Builder(EventJobService::class.java)
      .setInitialDelay(millis, TimeUnit.MILLISECONDS)
      .addTag(EVENT_BIRTHDAY)
      .setConstraints(getDefaultConstraints())
      .build()

    schedule(work)
  }

  @Deprecated("After S")
  fun scheduleMissedCall(number: String?) {
    if (number == null) return
    val time = prefs.missedReminderTime
    val millis = time * INTERVAL_MINUTE
    val bundle = Data.Builder()
      .putBoolean(ARG_MISSED, true)
      .build()

    val work = OneTimeWorkRequest.Builder(EventJobService::class.java)
      .setInitialDelay(millis, TimeUnit.MILLISECONDS)
      .addTag(number)
      .setInputData(bundle)
      .setConstraints(getDefaultConstraints())
      .build()

    schedule(work)
  }

  @Deprecated("After S")
  fun cancelMissedCall(number: String?) {
    if (number == null) return
    cancelReminder(number)
  }

  fun scheduleReminderRepeat(reminder: Reminder): Boolean {
    val minutes = prefs.notificationRepeatTime
    val millis = minutes * INTERVAL_MINUTE
    if (millis <= 0) {
      return false
    }
    Timber.d("scheduleReminderRepeat: $millis, ${reminder.uuId}")

    val bundle = Data.Builder()
      .putBoolean(ARG_REPEAT, true)
      .build()

    val work = OneTimeWorkRequest.Builder(EventJobService::class.java)
      .setInitialDelay(millis, TimeUnit.MILLISECONDS)
      .addTag(reminder.uuId)
      .setInputData(bundle)
      .setConstraints(getDefaultConstraints())
      .build()

    schedule(work)
    return true
  }

  fun scheduleReminderDelay(minutes: Int, uuId: String) {
    scheduleReminderDelay(INTERVAL_MINUTE * minutes, uuId)
  }

  fun scheduleReminderDelay(millis: Long, uuId: String) {
    if (millis <= 0) {
      return
    }
    Timber.d("scheduleReminderDelay: $millis, $uuId")

    val work = OneTimeWorkRequest.Builder(EventJobService::class.java)
      .setInitialDelay(millis, TimeUnit.MILLISECONDS)
      .addTag(uuId)
      .setConstraints(getDefaultConstraints())
      .build()

    schedule(work)
  }

  fun scheduleGpsDelay(reminder: Reminder): Boolean {
    val due = dateTimeManager.toMillis(reminder.eventTime)
    val millis = due - System.currentTimeMillis()
    if (due == 0L || millis <= 0) {
      return false
    }
    Timber.d("scheduleGpsDelay: $millis, ${reminder.uuId}")

    val bundle = Data.Builder()
      .putBoolean(ARG_LOCATION, true)
      .build()

    val work = OneTimeWorkRequest.Builder(EventJobService::class.java)
      .setInitialDelay(millis, TimeUnit.MILLISECONDS)
      .addTag(reminder.uuId)
      .setInputData(bundle)
      .setConstraints(getDefaultConstraints())
      .build()

    schedule(work)
    return true
  }

  fun scheduleReminder(reminder: Reminder?) {
    if (reminder == null) return
    var due = dateTimeManager.fromGmtToLocal(reminder.eventTime)
    Timber.d("scheduleReminder: noe -> ${dateTimeManager.logDateTime()}")
    if (due == null) {
      return
    }
    Timber.d("scheduleReminder: ${dateTimeManager.logDateTime(due)}")
    if (reminder.remindBefore != 0L) {
      due = due.minusMillis(reminder.remindBefore)
    }
    if (!Reminder.isBase(reminder.type, Reminder.BY_TIME)) {
      due = due.withSecond(0)
    }
    if (due == null) {
      return
    }
    var millis = dateTimeManager.toMillis(due) - System.currentTimeMillis()
    if (millis < 0) {
      millis = 100L
    }

    val work = OneTimeWorkRequest.Builder(EventJobService::class.java)
      .setInitialDelay(millis, TimeUnit.MILLISECONDS)
      .addTag(reminder.uuId)
      .setConstraints(getDefaultConstraints())
      .build()

    schedule(work)
  }

  private fun getDefaultConstraints(): Constraints {
    return Constraints.Builder()
      .setRequiresCharging(false)
      .setRequiresBatteryNotLow(false)
      .setRequiresStorageNotLow(false)
      .setRequiresDeviceIdle(false)
      .build()
  }

  fun cancelReminder(uuId: String) {
    Timber.i("cancelReminder: $uuId")
    WorkManager.getInstance(context).cancelAllWorkByTag(uuId)
  }

  fun scheduleSaveNewTask(googleTask: GoogleTask, uuId: String) {
    val work = OneTimeWorkRequest.Builder(SaveNewTaskWorker::class.java)
      .setInputData(
        Data.Builder().putString(Constants.INTENT_JSON, Gson().toJson(googleTask)).build()
      )
      .addTag(uuId)
      .build()

    schedule(work)
  }

  fun scheduleTaskDone(googleTask: GoogleTask, uuId: String) {
    val work = OneTimeWorkRequest.Builder(UpdateTaskWorker::class.java)
      .setInputData(
        Data.Builder()
          .putString(Constants.INTENT_JSON, Gson().toJson(googleTask))
          .putString(Constants.INTENT_STATUS, GTasks.TASKS_COMPLETE)
          .build()
      )
      .addTag(uuId)
      .build()

    schedule(work)
  }

  private fun schedule(workRequest: WorkRequest) {
    WorkManager.getInstance(context).enqueue(workRequest)
  }

  companion object {
    const val EVENT_BIRTHDAY = "event_birthday"
    const val EVENT_BIRTHDAY_PERMANENT = "event_birthday_permanent"
    const val EVENT_AUTO_SYNC = "event_auto_sync"
    const val EVENT_AUTO_BACKUP = "event_auto_backup"
    const val EVENT_CHECK = "event_check"
    private const val EVENT_CHECK_BIRTHDAYS = "event_check_birthday"

    const val ARG_LOCATION = "arg_location"
    @Deprecated("After S")
    const val ARG_MISSED = "arg_missed"
    const val ARG_REPEAT = "arg_repeated"

    private const val INTERVAL_MINUTE = 60 * 1000L
    private const val INTERVAL_HOUR = 60 * INTERVAL_MINUTE
  }
}
