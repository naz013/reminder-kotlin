package com.elementary.tasks.core.controller

import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.datetime.TimeUtil
import com.elementary.tasks.core.utils.launchIo

abstract class RepeatableEventManager(
  reminder: Reminder,
  appDb: AppDb,
  prefs: Prefs,
  private val googleCalendarUtils: GoogleCalendarUtils,
  notifier: Notifier,
  jobScheduler: JobScheduler,
  updatesHelper: UpdatesHelper,
  private val textProvider: TextProvider
) : EventManager(reminder, appDb, prefs, notifier, jobScheduler, updatesHelper) {

  protected fun enableReminder() {
    jobScheduler.scheduleReminder(reminder)
  }

  protected fun export() {
    if (reminder.exportToTasks) {
      val due = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
      val googleTask = GoogleTask()
      googleTask.listId = ""
      googleTask.status = GTasks.TASKS_NEED_ACTION
      googleTask.title = reminder.summary
      googleTask.dueDate = due
      googleTask.notes = textProvider.getText(R.string.from_reminder)
      googleTask.uuId = reminder.uuId
      jobScheduler.scheduleSaveNewTask(googleTask, reminder.uuId)
    }
    if (reminder.exportToCalendar) {
      if (prefs.isStockCalendarEnabled) {
        googleCalendarUtils.addEventToStock(
          reminder.summary,
          TimeUtil.getDateTimeFromGmt(reminder.eventTime)
        )
      }
      if (prefs.isCalendarEnabled) {
        googleCalendarUtils.addEvent(reminder)
      }
    }
  }

  private fun makeGoogleTaskDone() {
    if (reminder.exportToTasks) {
      launchIo {
        val googleTask = db.googleTasksDao().getByReminderId(reminder.uuId)
        if (googleTask != null && googleTask.status == GTasks.TASKS_NEED_ACTION) {
          jobScheduler.scheduleTaskDone(googleTask, reminder.uuId)
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
    notifier.cancel(reminder.uniqueId)
    jobScheduler.cancelReminder(reminder.uuId)
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
    jobScheduler.scheduleReminderDelay(delay, reminder.uuId)
  }
}
