package com.github.naz013.digest.work

import android.content.Context
import com.github.naz013.digest.DigestContentBuilder
import com.github.naz013.digest.DigestSummarizerChain
import com.github.naz013.digestapi.DigestSettingsGate
import com.github.naz013.logging.Logger
import com.github.naz013.logic.notificationaction.NotificationGateway
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.icon.DrawableCatalog
import com.github.naz013.workapi.BackgroundTask
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.TaskProgressReporter
import com.github.naz013.workapi.TaskResult
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

/**
 * Periodic 24h/1h-flex task (see [DigestSchedulerImpl][com.github.naz013.digest.DigestSchedulerImpl])
 * that self-gates on local time + a same-day dedup guard inside [run] - the same shape as
 * `HolidaySyncTask`/`CheckBirthdaysTask`. Keeps its own dedup date in a tiny local
 * `SharedPreferences` rather than `app`'s `Prefs`, so `digest` never depends on `app`.
 */
internal class DailyDigestTask(
  private val context: Context,
  private val digestSettingsGate: DigestSettingsGate,
  private val digestContentBuilder: DigestContentBuilder,
  private val digestSummarizerChain: DigestSummarizerChain,
  private val notificationGateway: NotificationGateway,
) : BackgroundTask {

  override suspend fun run(input: TaskData, progress: TaskProgressReporter): TaskResult {
    if (!digestSettingsGate.isDailyEnabled()) {
      Logger.i(TASK_KEY, "AI digest disabled, skipping")
      return TaskResult.Success
    }

    val today = LocalDate.now()
    if (LocalTime.now().hour < digestSettingsGate.preferredHour()) {
      Logger.i(TASK_KEY, "Before preferred hour, skipping this run")
      return TaskResult.Success
    }

    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    if (prefs.getString(KEY_LAST_POSTED_DATE, null) == today.toString()) {
      Logger.i(TASK_KEY, "Already posted today, skipping")
      return TaskResult.Success
    }

    val digestInput = digestContentBuilder.buildDaily(today)
    if (digestInput.isEmpty) {
      Logger.i(TASK_KEY, "Nothing to report today, skipping notification")
      return TaskResult.Success
    }

    val summary = digestSummarizerChain.summarize(digestInput)

    val builder = notificationGateway.builder(NotificationGateway.CHANNEL_SYSTEM)
    builder.setSmallIcon(DrawableCatalog.Fluent.CalendarAgenda)
    builder.setContentTitle(context.getString(R.string.ai_digest_notification_title))
    builder.setContentText(summary)
    builder.setAutoCancel(true)
    notificationGateway.notify(NOTIFICATION_ID, builder.build())

    prefs.edit().putString(KEY_LAST_POSTED_DATE, today.toString()).apply()
    Logger.i(TASK_KEY, "Posted daily digest notification")
    return TaskResult.Success
  }

  companion object {
    const val TASK_KEY = "digest_daily"
    private const val NOTIFICATION_ID = 985_612
    private const val PREFS_NAME = "digest_task_prefs"
    private const val KEY_LAST_POSTED_DATE = "daily_last_posted_date"
  }
}
