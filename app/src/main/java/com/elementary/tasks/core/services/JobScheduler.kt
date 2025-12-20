package com.elementary.tasks.core.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.elementary.tasks.settings.birthday.work.CheckBirthdaysWorker
import com.elementary.tasks.core.services.alarm.AlarmReceiver
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.googletasks.work.SaveNewTaskWorker
import com.elementary.tasks.googletasks.work.UpdateTaskWorker
import com.elementary.tasks.reminder.scheduling.alarmmanager.EventDateTimeCalculator
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.common.intent.PendingIntentWrapper
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.Reminder
import com.github.naz013.feature.common.android.SystemServiceProvider
import com.github.naz013.logging.Logger
import com.google.gson.Gson
import java.util.Calendar
import java.util.concurrent.TimeUnit

class JobScheduler(
  private val context: Context,
  private val prefs: Prefs,
  private val dateTimeManager: DateTimeManager,
  private val systemServiceProvider: SystemServiceProvider,
  private val eventDateTimeCalculator: EventDateTimeCalculator,
) {

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
    Logger.i(TAG, "Scheduled birthday check.")
  }

  fun cancelBirthdaysCheck() {
    cancelReminder(EVENT_CHECK_BIRTHDAYS)
    Logger.w(TAG, "Cancelled birthday check.")
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

  fun scheduleReminderRepeat(reminder: Reminder): Boolean {
    val minutes = prefs.notificationRepeatTime
    val millis = System.currentTimeMillis() + (minutes * INTERVAL_MINUTE)
    if (millis <= 0) {
      return false
    }
    Logger.d("scheduleReminderRepeat: $millis, ${reminder.uuId}")

    scheduleWithAlarm(
      action = AlarmReceiver.ACTION_REMINDER_REPEAT,
      bundle = Bundle().apply {
        putString(IntentKeys.INTENT_ID, reminder.uuId)
      },
      millis = millis,
      requestCode = reminder.uniqueId
    )
    return true
  }

  fun scheduleReminderDelay(minutes: Int, uuId: String, requestCode: Int) {
    scheduleReminderDelay(INTERVAL_MINUTE * minutes, uuId, requestCode)
  }

  fun scheduleReminderDelay(millis: Long, uuId: String, requestCode: Int) {
    if (millis <= 0) {
      return
    }
    Logger.d("scheduleReminderDelay: $millis, $uuId")

    scheduleWithAlarm(
      action = AlarmReceiver.ACTION_REMINDER,
      bundle = Bundle().apply {
        putString(IntentKeys.INTENT_ID, uuId)
      },
      millis = System.currentTimeMillis() + millis,
      requestCode = requestCode
    )
  }

  fun scheduleGpsDelay(reminder: Reminder): Boolean {
    val millis = dateTimeManager.toMillis(reminder.eventTime)
    if (millis <= 0) {
      return false
    }
    Logger.d("scheduleGpsDelay: $millis, ${reminder.uuId}")

    scheduleWithAlarm(
      action = AlarmReceiver.ACTION_REMINDER_GPS,
      bundle = Bundle().apply {
        putString(IntentKeys.INTENT_ID, reminder.uuId)
      },
      millis = millis,
      requestCode = reminder.uniqueId
    )
    return true
  }

  fun scheduleReminder(reminder: Reminder?) {
    if (reminder == null) {
      Logger.w(TAG, "Cannot schedule null reminder")
      return
    }
    val millis = eventDateTimeCalculator.calculateEventDateTime(reminder) ?: run {
      Logger.e(TAG, "Cannot calculate event date time for reminder: ${reminder.uuId}")
      return
    }

    scheduleWithAlarm(
      action = AlarmReceiver.ACTION_REMINDER,
      bundle = Bundle().apply {
        putString(IntentKeys.INTENT_ID, reminder.uuId)
      },
      millis = millis,
      requestCode = reminder.uniqueId
    )
  }

  private fun scheduleWithAlarm(
    action: String,
    bundle: Bundle,
    millis: Long,
    requestCode: Int
  ) {
    val intent = Intent(context, AlarmReceiver::class.java)
    intent.action = action
    intent.putExtras(bundle)
    val pendingIntent = PendingIntentWrapper.getBroadcast(
      context = context,
      requestCode = requestCode,
      intent = intent,
      flags = PendingIntent.FLAG_CANCEL_CURRENT,
      ignoreIn13 = false
    )
    systemServiceProvider.provideAlarmManager()?.setExactAndAllowWhileIdle(
      AlarmManager.RTC_WAKEUP,
      millis,
      pendingIntent
    )
  }

  private fun getDefaultConstraints(): Constraints {
    return Constraints.Builder()
      .setRequiresCharging(false)
      .setRequiresBatteryNotLow(false)
      .setRequiresStorageNotLow(false)
      .setRequiresDeviceIdle(false)
      .build()
  }

  private fun cancelReminder(uuId: String) {
    Logger.i("cancelReminder: uuId=$uuId")
    WorkManager.getInstance(context).cancelAllWorkByTag(uuId)
  }

  fun cancelReminder(requestCode: Int) {
    Logger.i("cancelReminder: requestCode=$requestCode")
    val intent = Intent(context, AlarmReceiver::class.java)
    val pendingIntent = PendingIntentWrapper.getBroadcast(
      context = context,
      requestCode = requestCode,
      intent = intent,
      flags = PendingIntent.FLAG_CANCEL_CURRENT,
      ignoreIn13 = false
    )
    systemServiceProvider.provideAlarmManager()?.cancel(pendingIntent)
  }

  fun scheduleSaveNewTask(googleTask: GoogleTask, uuId: String) {
    val work = OneTimeWorkRequest.Builder(SaveNewTaskWorker::class.java)
      .setInputData(
        Data.Builder().putString(IntentKeys.INTENT_JSON, Gson().toJson(googleTask)).build()
      )
      .addTag(uuId)
      .build()

    schedule(work)
  }

  fun scheduleTaskDone(googleTask: GoogleTask, uuId: String) {
    val work = OneTimeWorkRequest.Builder(UpdateTaskWorker::class.java)
      .setInputData(
        Data.Builder()
          .putString(IntentKeys.INTENT_JSON, Gson().toJson(googleTask))
          .putString(IntentKeys.INTENT_STATUS, GoogleTask.TASKS_COMPLETE)
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
    const val EVENT_AUTO_BACKUP = "event_auto_backup"
    private const val EVENT_CHECK_BIRTHDAYS = "event_check_birthday"
    private const val TAG = "JobScheduler"

    private const val INTERVAL_MINUTE = 60 * 1000L
    private const val INTERVAL_HOUR = 60 * INTERVAL_MINUTE
  }
}
