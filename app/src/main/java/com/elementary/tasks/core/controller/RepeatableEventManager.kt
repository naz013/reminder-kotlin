package com.elementary.tasks.core.controller

import com.elementary.tasks.R
import com.elementary.tasks.core.appwidgets.UpdatesHelper
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.dao.GoogleTasksDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.launchIo
import com.elementary.tasks.core.utils.params.Prefs

abstract class RepeatableEventManager(
  reminder: Reminder,
  reminderDao: ReminderDao,
  prefs: Prefs,
  private val googleCalendarUtils: GoogleCalendarUtils,
  notifier: Notifier,
  private val jobScheduler: JobScheduler,
  updatesHelper: UpdatesHelper,
  private val textProvider: TextProvider,
  private val dateTimeManager: DateTimeManager,
  private val googleTasksDao: GoogleTasksDao
) : EventManager(reminder, reminderDao, prefs, notifier, updatesHelper) {

  protected fun enableReminder() {
    jobScheduler.scheduleReminder(reminder)
  }

  protected fun export() {
    if (reminder.exportToTasks) {
      val due = dateTimeManager.toMillis(reminder.eventTime)
      val googleTask = GoogleTask()
      googleTask.listId = reminder.taskListId ?: ""
      googleTask.status = GTasks.TASKS_NEED_ACTION
      googleTask.title = reminder.summary
      googleTask.dueDate = due
      googleTask.notes = reminder.description ?: textProvider.getText(R.string.from_reminder)
      googleTask.uuId = reminder.uuId
      jobScheduler.scheduleSaveNewTask(googleTask, reminder.uuId)
    }
    if (reminder.exportToCalendar) {
      if (prefs.isStockCalendarEnabled) {
        googleCalendarUtils.addEventToStock(
          reminder.summary,
          dateTimeManager.toMillis(reminder.eventTime)
        )
      }
      if (prefs.isCalendarEnabled || reminder.version == Reminder.Version.V3) {
        googleCalendarUtils.addEvent(reminder)
      }
    }
  }

  private fun makeGoogleTaskDone() {
    if (reminder.exportToTasks) {
      launchIo {
        val googleTask = googleTasksDao.getByReminderId(reminder.uuId)
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
    jobScheduler.cancelReminder(reminder.uniqueId)
    return true
  }

  override fun disable(): Boolean {
    reminder.isActive = false
    if (prefs.moveCompleted) {
      reminder.isRemoved = true
    }
    save()
    makeGoogleTaskDone()
    return pause()
  }

  override fun setDelay(delay: Int) {
    jobScheduler.scheduleReminderDelay(delay, reminder.uuId, reminder.uniqueId)
  }
}
