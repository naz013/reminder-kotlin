package com.elementary.tasks.pomodoro

import android.app.Notification
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import androidx.annotation.StringRes
import com.elementary.tasks.R
import com.github.naz013.feature.pomodoro.engine.PomodoroMode

internal class PomodoroNotificationTexts(
  val title: String,
  val toggleActionLabel: String,
  val stopActionLabel: String,
)

@StringRes
internal fun PomodoroMode.labelRes(): Int = when (this) {
  PomodoroMode.Work -> R.string.pomodoro_work
  PomodoroMode.Break -> R.string.pomodoro_break
  PomodoroMode.LongBreak -> R.string.pomodoro_long_break
  PomodoroMode.Idle -> R.string.pomodoro_focus_timer
}

internal class PomodoroNotificationFactory {

  fun build(
    builder: NotificationCompat.Builder,
    plan: PomodoroNotificationPlan,
    texts: PomodoroNotificationTexts,
    toggleIntent: PendingIntent,
    stopIntent: PendingIntent,
  ): Notification {
    apply(builder, plan, texts, toggleIntent, stopIntent)
    return builder.build()
  }

  fun apply(
    builder: NotificationCompat.Builder,
    plan: PomodoroNotificationPlan,
    texts: PomodoroNotificationTexts,
    toggleIntent: PendingIntent,
    stopIntent: PendingIntent,
  ) {
    builder.setContentTitle(texts.title)
    builder.setSmallIcon(R.drawable.ic_fluent_clock_alarm)
    builder.setOngoing(true)
    builder.setOnlyAlertOnce(true)
    builder.priority = NotificationCompat.PRIORITY_LOW
    applyTime(builder, plan)
    if (plan.promoted) {
      applyLiveUpdate(builder, plan)
    }
    builder.addAction(0, texts.toggleActionLabel, toggleIntent)
    builder.addAction(0, texts.stopActionLabel, stopIntent)
  }

  private fun applyTime(builder: NotificationCompat.Builder, plan: PomodoroNotificationPlan) {
    if (plan.useChronometer) {
      builder.setShowWhen(true)
      builder.setWhen(plan.endTimeMillis)
      builder.setUsesChronometer(true)
      builder.setChronometerCountDown(true)
    } else {
      builder.setShowWhen(false)
      builder.setUsesChronometer(false)
      builder.setContentText(plan.timeLabel)
    }
  }

  private fun applyLiveUpdate(builder: NotificationCompat.Builder, plan: PomodoroNotificationPlan) {
    builder.setRequestPromotedOngoing(true)
    if (!plan.useChronometer) {
      builder.setShortCriticalText(plan.timeLabel)
    }
    if (plan.showProgress) {
      val style = NotificationCompat.ProgressStyle()
        .addProgressSegment(NotificationCompat.ProgressStyle.Segment(plan.progressMax))
        .setProgress(plan.progressElapsed)
      builder.setStyle(style)
    }
  }
}
