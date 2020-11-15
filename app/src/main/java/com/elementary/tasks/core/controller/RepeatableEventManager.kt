package com.elementary.tasks.core.controller

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.EventJobScheduler
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.launchIo
import com.elementary.tasks.google_tasks.work.SaveNewTaskWorker
import com.elementary.tasks.google_tasks.work.UpdateTaskWorker
import com.google.gson.Gson

abstract class RepeatableEventManager(
  reminder: Reminder,
  appDb: AppDb,
  prefs: Prefs,
  calendarUtils: CalendarUtils,
  context: Context
) : EventManager(reminder, appDb, prefs, calendarUtils, context) {

  protected fun enableReminder() {
    EventJobScheduler.scheduleReminder(reminder)
  }

  protected fun export() {
    if (reminder.exportToTasks) {
      val due = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
      val googleTask = GoogleTask()
      googleTask.listId = ""
      googleTask.status = GTasks.TASKS_NEED_ACTION
      googleTask.title = reminder.summary
      googleTask.dueDate = due
      googleTask.notes = context.getString(R.string.from_reminder)
      googleTask.uuId = reminder.uuId
      val work = OneTimeWorkRequest.Builder(SaveNewTaskWorker::class.java)
        .setInputData(Data.Builder().putString(Constants.INTENT_JSON, Gson().toJson(googleTask)).build())
        .addTag(reminder.uuId)
        .build()
      WorkManager.getInstance(context).enqueue(work)
    }
    if (reminder.exportToCalendar) {
      if (prefs.isStockCalendarEnabled) {
        calendarUtils.addEventToStock(reminder.summary, TimeUtil.getDateTimeFromGmt(reminder.eventTime))
      }
      if (prefs.isCalendarEnabled) {
        calendarUtils.addEvent(reminder)
      }
    }
  }

  private fun makeGoogleTaskDone() {
    if (reminder.exportToTasks) {
      launchIo {
        val googleTask = AppDb.getAppDatabase(context).googleTasksDao().getByReminderId(reminder.uuId)
        if (googleTask != null && googleTask.status == GTasks.TASKS_NEED_ACTION) {
          val work = OneTimeWorkRequest.Builder(UpdateTaskWorker::class.java)
            .setInputData(
              Data.Builder()
                .putString(Constants.INTENT_JSON, Gson().toJson(googleTask))
                .putString(Constants.INTENT_STATUS, GTasks.TASKS_COMPLETE)
                .build())
            .addTag(reminder.uuId)
            .build()
          WorkManager.getInstance(context).enqueue(work)
        }
      }
    }
  }

  override fun resume(): Boolean {
    if (reminder.isActive) {
      enableReminder()
    }
    return true
  }

  override fun pause(): Boolean {
    Notifier.hideNotification(context, reminder.uniqueId)
    EventJobScheduler.cancelReminder(reminder.uuId)
    return true
  }

  override fun stop(): Boolean {
    reminder.isActive = false
    if (prefs.moveCompleted) {
      reminder.isRemoved = true
    }
    save()
    makeGoogleTaskDone()
    return pause()
  }

  override fun setDelay(delay: Int) {
    EventJobScheduler.scheduleReminderDelay(delay, reminder.uuId)
  }
}
