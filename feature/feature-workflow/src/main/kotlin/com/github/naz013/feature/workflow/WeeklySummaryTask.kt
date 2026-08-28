package com.github.naz013.feature.workflow

import android.content.Context
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger
import com.github.naz013.logic.notificationaction.NotificationGateway
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.ui.common.icon.DrawableCatalog
import com.github.naz013.workapi.BackgroundTask
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.TaskProgressReporter
import com.github.naz013.workapi.TaskResult
import org.threeten.bp.LocalDateTime

/**
 * Counts reminders completed in the last 7 days and posts a single summary notification - the
 * seeded "Weekly reminder completion summary" template ([WorkflowRulesUtil]) invokes this via
 * [com.github.naz013.domain.workflow.WorkflowAction.RunBackgroundTask], the same escape hatch
 * every other workflow action the engine can't apply itself inline goes through. Skips posting
 * when nothing was completed, rather than sending an empty "0 reminders" notification every week.
 */
class WeeklySummaryTask(
  private val context: Context,
  private val reminderV2Repository: ReminderV2Repository,
  private val notificationGateway: NotificationGateway,
) : BackgroundTask {
  override suspend fun run(
    input: TaskData,
    progress: TaskProgressReporter
  ): TaskResult {
    val since = LocalDateTime.now().minusDays(DAYS_IN_WINDOW)
    val allCompletedReminders = reminderV2Repository.getAll(active = false, removed = false) +
      reminderV2Repository.getAll(active = false, removed = true)
    val completedCount = allCompletedReminders.count { referenceDateTime(it).isAfter(since) }

    if (completedCount == 0) {
      Logger.i(TASK_KEY, "No reminders completed in the last 7 days, skipping summary notification.")
      return TaskResult.Success
    }

    val builder = notificationGateway.builder(NotificationGateway.CHANNEL_SYSTEM)
    builder.setSmallIcon(DrawableCatalog.Fluent.ArrowRepeatAll)
    builder.setContentTitle(context.getString(R.string.workflow_weekly_summary_title))
    builder.setContentText(context.getString(R.string.workflow_weekly_summary_body, completedCount))
    builder.setAutoCancel(true)
    notificationGateway.notify(NOTIFICATION_ID, builder.build())

    Logger.i(TASK_KEY, "Posted weekly summary notification, completedCount=$completedCount")
    return TaskResult.Success
  }

  private fun referenceDateTime(reminder: ReminderV2): LocalDateTime =
    reminder.schedule.updatedAt ?: reminder.schedule.startDateTime

  companion object {
    const val TASK_KEY = "run_weekly_summary"
    private const val DAYS_IN_WINDOW = 7L
    private const val NOTIFICATION_ID = 985_611
  }
}
