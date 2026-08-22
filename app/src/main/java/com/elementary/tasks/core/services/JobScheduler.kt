package com.elementary.tasks.core.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.elementary.tasks.core.services.alarm.AlarmReceiver
import com.elementary.tasks.core.services.event.AutoBackupEventTask
import com.elementary.tasks.core.services.event.BirthdayEventTask
import com.elementary.tasks.core.services.event.BirthdayPermanentEventTask
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.logic.reminder.scheduling.EventDateTimeCalculatorV2
import com.github.naz013.feature.birthday.settings.work.CheckBirthdaysTask
import com.github.naz013.feature.workflow.RunWorkflowRulesTask
import com.github.naz013.feature.workflow.RunWorkflowUnacknowledgedRulesTask
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.common.intent.PendingIntentWrapper
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.feature.common.android.SystemServiceProvider
import com.github.naz013.feature.googletask.work.SaveNewTaskTask
import com.github.naz013.feature.googletask.work.UpdateTaskTask
import com.github.naz013.feature.routine.RoutineRecurrenceResetTask
import com.github.naz013.logging.Logger
import com.github.naz013.scheduler.JobSchedulerApi
import com.github.naz013.workapi.PeriodicWorkRequest
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.WorkRequest
import com.github.naz013.workapi.WorkScheduler
import com.google.gson.Gson
import java.util.Calendar
import java.util.concurrent.TimeUnit

class JobScheduler(
  private val context: Context,
  private val prefs: Prefs,
  private val dateTimeManager: DateTimeManager,
  private val systemServiceProvider: SystemServiceProvider,
  private val eventDateTimeCalculatorV2: EventDateTimeCalculatorV2,
  private val workScheduler: WorkScheduler,
) : JobSchedulerApi {

  override fun scheduleBirthdaysCheck() {
    workScheduler.enqueuePeriodic(
      PeriodicWorkRequest(
        taskKey = CheckBirthdaysTask.TASK_KEY,
        tag = EVENT_CHECK_BIRTHDAYS,
        repeatIntervalMillis = TimeUnit.HOURS.toMillis(24),
        flexIntervalMillis = TimeUnit.HOURS.toMillis(1),
      ),
    )
    Logger.i(TAG, "Scheduled birthday check.")
  }

  override fun cancelBirthdaysCheck() {
    cancelReminder(EVENT_CHECK_BIRTHDAYS)
    Logger.w(TAG, "Cancelled birthday check.")
  }

  override fun scheduleWorkflowRulesCheck() {
    workScheduler.enqueuePeriodic(
      PeriodicWorkRequest(
        taskKey = RunWorkflowRulesTask.TASK_KEY,
        tag = EVENT_WORKFLOW_RULES_CHECK,
        repeatIntervalMillis = TimeUnit.HOURS.toMillis(24),
        flexIntervalMillis = TimeUnit.HOURS.toMillis(1),
      ),
    )
    Logger.i(TAG, "Scheduled workflow rules check.")
  }

  override fun scheduleWorkflowUnacknowledgedCheck() {
    workScheduler.enqueuePeriodic(
      PeriodicWorkRequest(
        taskKey = RunWorkflowUnacknowledgedRulesTask.TASK_KEY,
        tag = EVENT_WORKFLOW_UNACKNOWLEDGED_RULES_CHECK,
        repeatIntervalMillis = TimeUnit.MINUTES.toMillis(30),
        flexIntervalMillis = TimeUnit.MINUTES.toMillis(5),
      ),
    )
    Logger.i(TAG, "Scheduled workflow unacknowledged-reminder rules check.")
  }

  override fun scheduleRoutineRecurrenceResetCheck() {
    workScheduler.enqueuePeriodic(
      PeriodicWorkRequest(
        taskKey = RoutineRecurrenceResetTask.TASK_KEY,
        tag = EVENT_ROUTINE_RECURRENCE_RESET_CHECK,
        repeatIntervalMillis = TimeUnit.HOURS.toMillis(24),
        flexIntervalMillis = TimeUnit.HOURS.toMillis(1),
      ),
    )
    Logger.i(TAG, "Scheduled routine recurrence reset check.")
  }

  override fun scheduleBirthdayPermanent() {
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

    workScheduler.enqueue(
      WorkRequest(
        taskKey = BirthdayPermanentEventTask.TASK_KEY,
        tag = EVENT_BIRTHDAY_PERMANENT,
        initialDelayMillis = millis - System.currentTimeMillis(),
      ),
    )
  }

  override fun cancelBirthdayPermanent() {
    cancelReminder(EVENT_BIRTHDAY_PERMANENT)
  }

  override fun scheduleAutoBackup() {
    val interval = prefs.autoBackupState
    if (interval <= 0) {
      cancelAutoBackup()
      return
    }
    val millis = INTERVAL_HOUR * interval

    workScheduler.enqueue(
      WorkRequest(
        taskKey = AutoBackupEventTask.TASK_KEY,
        tag = EVENT_AUTO_BACKUP,
        initialDelayMillis = millis,
      ),
    )
  }

  private fun cancelAutoBackup() {
    cancelReminder(EVENT_AUTO_BACKUP)
  }

  override fun cancelDailyBirthday() {
    cancelReminder(EVENT_BIRTHDAY)
  }

  override fun scheduleDailyBirthday() {
    val millis = dateTimeManager.getMillisToBirthdayTime()
    if (millis <= 0) return

    workScheduler.enqueue(
      WorkRequest(
        taskKey = BirthdayEventTask.TASK_KEY,
        tag = EVENT_BIRTHDAY,
        initialDelayMillis = millis,
      ),
    )
  }

  override fun scheduleReminderRepeat(reminderV2: ReminderV2): Boolean {
    val minutes = prefs.notificationRepeatTime
    val millis = System.currentTimeMillis() + (minutes * INTERVAL_MINUTE)
    if (millis <= 0) {
      return false
    }
    Logger.d(TAG, "scheduleReminderRepeat: $millis, ${reminderV2.uuId}")

    scheduleWithAlarm(
      action = AlarmReceiver.ACTION_REMINDER_REPEAT,
      bundle =
        Bundle().apply {
          putString(IntentKeys.INTENT_ID, reminderV2.uuId)
        },
      millis = millis,
      requestCode = reminderV2.uniqueId,
    )
    return true
  }

  override fun scheduleReminderDelay(
    minutes: Int,
    uuId: String,
    requestCode: Int,
  ) {
    scheduleReminderDelay(INTERVAL_MINUTE * minutes, uuId, requestCode)
  }

  override fun scheduleReminderDelay(
    millis: Long,
    uuId: String,
    requestCode: Int,
  ) {
    if (millis <= 0) {
      return
    }
    Logger.d(TAG, "scheduleReminderDelay: $millis, $uuId")

    scheduleWithAlarm(
      action = AlarmReceiver.ACTION_REMINDER,
      bundle =
        Bundle().apply {
          putString(IntentKeys.INTENT_ID, uuId)
        },
      millis = System.currentTimeMillis() + millis,
      requestCode = requestCode,
    )
  }

  override fun scheduleGpsDelay(reminderV2: ReminderV2): Boolean {
    val millis = reminderV2.schedule.eventDateTime?.let { dateTimeManager.toMillis(dateTimeManager.utcToLocal(it)) } ?: 0L
    if (millis <= 0) {
      return false
    }
    Logger.d(TAG, "scheduleGpsDelay: $millis, ${reminderV2.uuId}")

    scheduleWithAlarm(
      action = AlarmReceiver.ACTION_REMINDER_GPS,
      bundle =
        Bundle().apply {
          putString(IntentKeys.INTENT_ID, reminderV2.uuId)
        },
      millis = millis,
      requestCode = reminderV2.uniqueId,
    )
    return true
  }

  override fun scheduleReminder(reminderV2: ReminderV2?) {
    if (reminderV2 == null) {
      Logger.w(TAG, "Cannot schedule null reminder")
      return
    }
    val millis =
      eventDateTimeCalculatorV2.calculateEventDateTime(reminderV2) ?: run {
        Logger.e(TAG, "Cannot calculate event date time for reminder: ${reminderV2.uuId}")
        return
      }

    scheduleWithAlarm(
      action = AlarmReceiver.ACTION_REMINDER,
      bundle =
        Bundle().apply {
          putString(IntentKeys.INTENT_ID, reminderV2.uuId)
        },
      millis = millis,
      requestCode = reminderV2.uniqueId,
    )
  }

  private fun scheduleWithAlarm(
    action: String,
    bundle: Bundle,
    millis: Long,
    requestCode: Int,
  ) {
    val intent = Intent(context, AlarmReceiver::class.java)
    intent.action = action
    intent.putExtras(bundle)
    val pendingIntent =
      PendingIntentWrapper.getBroadcast(
        context = context,
        requestCode = requestCode,
        intent = intent,
        flags = PendingIntent.FLAG_CANCEL_CURRENT,
        ignoreIn13 = false,
      )
    val alarmManager = systemServiceProvider.provideAlarmManager()
    if (alarmManager == null) {
      Logger.e(TAG, "Cannot schedule alarm for action=$action: AlarmManager is unavailable")
      return
    }
    try {
      if (canScheduleExactAlarms(alarmManager)) {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
      } else {
        Logger.w(TAG, "Exact alarms are not permitted, scheduling an inexact alarm for action=$action instead")
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
      }
    } catch (e: SecurityException) {
      Logger.e(TAG, "Failed to schedule exact alarm for action=$action, falling back to an inexact alarm", e)
      alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
    }
  }

  private fun canScheduleExactAlarms(alarmManager: AlarmManager): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      alarmManager.canScheduleExactAlarms()
    } else {
      true
    }
  }

  private fun cancelReminder(uuId: String) {
    Logger.i(TAG, "cancelReminder: uuId=$uuId")
    workScheduler.cancelByTag(uuId)
  }

  override fun cancelReminder(requestCode: Int) {
    Logger.i(TAG, "cancelReminder: requestCode=$requestCode")
    val intent = Intent(context, AlarmReceiver::class.java)
    val pendingIntent =
      PendingIntentWrapper.getBroadcast(
        context = context,
        requestCode = requestCode,
        intent = intent,
        flags = PendingIntent.FLAG_CANCEL_CURRENT,
        ignoreIn13 = false,
      )
    systemServiceProvider.provideAlarmManager()?.cancel(pendingIntent)
  }

  override fun scheduleSaveNewTask(
    googleTask: GoogleTask,
    uuId: String,
  ) {
    workScheduler.enqueue(
      WorkRequest(
        taskKey = SaveNewTaskTask.TASK_KEY,
        tag = uuId,
        input = TaskData.builder().putString(IntentKeys.INTENT_JSON, Gson().toJson(googleTask)).build(),
      ),
    )
  }

  override fun scheduleTaskDone(
    googleTask: GoogleTask,
    uuId: String,
  ) {
    workScheduler.enqueue(
      WorkRequest(
        taskKey = UpdateTaskTask.TASK_KEY,
        tag = uuId,
        input =
          TaskData
            .builder()
            .putString(IntentKeys.INTENT_JSON, Gson().toJson(googleTask))
            .putString(IntentKeys.INTENT_STATUS, GoogleTask.TASKS_COMPLETE)
            .build(),
      ),
    )
  }

  companion object {
    private const val EVENT_BIRTHDAY = "event_birthday"
    private const val EVENT_BIRTHDAY_PERMANENT = "event_birthday_permanent"
    private const val EVENT_AUTO_BACKUP = "event_auto_backup"
    private const val EVENT_CHECK_BIRTHDAYS = "event_check_birthday"
    private const val EVENT_WORKFLOW_RULES_CHECK = "event_workflow_rules_check"
    private const val EVENT_WORKFLOW_UNACKNOWLEDGED_RULES_CHECK = "event_workflow_unacknowledged_rules_check"
    private const val EVENT_ROUTINE_RECURRENCE_RESET_CHECK = "event_routine_recurrence_reset_check"
    private const val TAG = "JobScheduler"

    private const val INTERVAL_MINUTE = 60 * 1000L
    private const val INTERVAL_HOUR = 60 * INTERVAL_MINUTE
  }
}
