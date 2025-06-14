package com.elementary.tasks.core.controller

import com.elementary.tasks.R
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.feature.common.coroutine.invokeSuspend
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.Notifier
import com.github.naz013.common.datetime.DateTimeManager
import com.elementary.tasks.core.utils.launchIo
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.Reminder
import com.github.naz013.common.TextProvider
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.ReminderRepository

abstract class RepeatableEventManager(
  reminder: Reminder,
  reminderRepository: ReminderRepository,
  prefs: Prefs,
  private val googleCalendarUtils: GoogleCalendarUtils,
  notifier: Notifier,
  private val jobScheduler: JobScheduler,
  appWidgetUpdater: AppWidgetUpdater,
  private val textProvider: TextProvider,
  private val dateTimeManager: DateTimeManager,
  private val googleTaskRepository: GoogleTaskRepository
) : EventManager(reminder, reminderRepository, prefs, notifier, appWidgetUpdater) {

  protected fun enableReminder() {
    jobScheduler.scheduleReminder(reminder)
  }

  protected fun export() {
    if (reminder.exportToTasks) {
      val due = dateTimeManager.toMillis(reminder.eventTime)
      val googleTask = GoogleTask()
      googleTask.listId = reminder.taskListId ?: ""
      googleTask.status = GoogleTask.TASKS_NEED_ACTION
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
        invokeSuspend { googleCalendarUtils.addEvent(reminder) }
      }
    }
  }

  private fun makeGoogleTaskDone() {
    if (reminder.exportToTasks) {
      launchIo {
        val googleTask = googleTaskRepository.getByReminderId(reminder.uuId)
        if (googleTask != null && googleTask.status == GoogleTask.TASKS_NEED_ACTION) {
          jobScheduler.scheduleTaskDone(googleTask, reminder.uuId)
        }
      }
    }
  }

  override fun resume(): Boolean {
    Logger.d("RepeatableEventManager", "Resume reminder, id: ${reminder.uuId}")
    if (reminder.isActive) {
      enableReminder()
    }
    return true
  }

  override fun pause(): Boolean {
    Logger.d("RepeatableEventManager", "Pause reminder, id: ${reminder.uuId}")
    notifier.cancel(reminder.uniqueId)
    jobScheduler.cancelReminder(reminder.uniqueId)
    return true
  }

  override fun disable(): Boolean {
    Logger.d("RepeatableEventManager", "Disable reminder, id: ${reminder.uuId}")
    reminder.isActive = false
    if (prefs.moveCompleted) {
      reminder.isRemoved = true
    }
    save()
    makeGoogleTaskDone()
    return pause()
  }

  override fun setDelay(delay: Int) {
    Logger.d("RepeatableEventManager", "Set delay reminder, id: ${reminder.uuId}")
    jobScheduler.scheduleReminderDelay(delay, reminder.uuId, reminder.uniqueId)
  }
}
