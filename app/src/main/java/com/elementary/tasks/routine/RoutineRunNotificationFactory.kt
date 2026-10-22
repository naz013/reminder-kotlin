package com.elementary.tasks.routine

import android.app.Notification
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import com.github.naz013.ui.common.icon.DrawableCatalog

internal class RoutineRunNotificationTexts(
  val stepOfLabel: String,
  val pauseActionLabel: String,
  val resumeActionLabel: String,
  val skipActionLabel: String,
  val stopActionLabel: String,
)

internal class RoutineRunNotificationIntents(
  val content: PendingIntent?,
  val toggle: PendingIntent,
  val skip: PendingIntent,
  val stop: PendingIntent,
)

internal class RoutineRunNotificationFactory {

  fun build(
    builder: NotificationCompat.Builder,
    plan: RoutineRunNotificationPlan,
    texts: RoutineRunNotificationTexts,
    intents: RoutineRunNotificationIntents,
  ): Notification {
    apply(builder, plan, texts, intents)
    return builder.build()
  }

  fun apply(
    builder: NotificationCompat.Builder,
    plan: RoutineRunNotificationPlan,
    texts: RoutineRunNotificationTexts,
    intents: RoutineRunNotificationIntents,
  ) {
    builder.setContentTitle(plan.routineTitle)
    builder.setSmallIcon(DrawableCatalog.Builder.Timer)
    builder.setOngoing(true)
    builder.setOnlyAlertOnce(true)
    builder.priority = NotificationCompat.PRIORITY_LOW
    builder.setSubText(texts.stepOfLabel)
    builder.setContentText(plan.pausedTimeLabel?.let { "${plan.stepTitle} - $it" } ?: plan.stepTitle)
    intents.content?.let { builder.setContentIntent(it) }
    applyTime(builder, plan)
    if (plan.promoted) {
      applyLiveUpdate(builder, plan)
    }
    if (plan.showToggleAction) {
      val label = if (plan.isPaused) texts.resumeActionLabel else texts.pauseActionLabel
      builder.addAction(0, label, intents.toggle)
    }
    builder.addAction(0, texts.skipActionLabel, intents.skip)
    builder.addAction(0, texts.stopActionLabel, intents.stop)
  }

  private fun applyTime(builder: NotificationCompat.Builder, plan: RoutineRunNotificationPlan) {
    if (plan.useChronometer) {
      builder.setShowWhen(true)
      builder.setWhen(plan.endTimeMillis)
      builder.setUsesChronometer(true)
      builder.setChronometerCountDown(true)
    } else {
      builder.setShowWhen(false)
      builder.setUsesChronometer(false)
    }
  }

  private fun applyLiveUpdate(builder: NotificationCompat.Builder, plan: RoutineRunNotificationPlan) {
    builder.setRequestPromotedOngoing(true)
    plan.pausedTimeLabel?.let { builder.setShortCriticalText(it) }
    if (plan.showProgress) {
      val style = NotificationCompat.ProgressStyle().setProgress(plan.completedCount)
      repeat(plan.stepCount) {
        style.addProgressSegment(NotificationCompat.ProgressStyle.Segment(1))
      }
      builder.setStyle(style)
    }
  }
}
