package com.github.naz013.scheduler

import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.reminder.v2.ReminderV2

interface JobSchedulerApi {

  fun scheduleBirthdaysCheck()

  fun cancelBirthdaysCheck()

  fun scheduleWorkflowRulesCheck()

  fun scheduleWorkflowUnacknowledgedCheck()

  fun scheduleRoutineRecurrenceResetCheck()

  fun scheduleBirthdayPermanent()

  fun cancelBirthdayPermanent()

  fun scheduleAutoBackup()

  fun cancelDailyBirthday()

  fun scheduleDailyBirthday()

  fun scheduleReminderRepeat(
    reminderV2: ReminderV2,
    repeatCount: Int,
  ): Boolean

  fun scheduleReminderDelay(
    minutes: Int,
    uuId: String,
    requestCode: Int,
  )

  fun scheduleReminderDelay(
    millis: Long,
    uuId: String,
    requestCode: Int,
  )

  fun scheduleGpsDelay(reminderV2: ReminderV2): Boolean

  fun scheduleReminder(reminderV2: ReminderV2?)

  fun cancelReminder(requestCode: Int)

  fun scheduleSaveNewTask(
    googleTask: GoogleTask,
    uuId: String,
  )

  fun scheduleTaskDone(
    googleTask: GoogleTask,
    uuId: String,
  )
}
