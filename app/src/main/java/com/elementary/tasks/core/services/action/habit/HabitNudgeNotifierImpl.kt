package com.elementary.tasks.core.services.action.habit

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.elementary.tasks.core.services.HabitActionReceiver
import com.elementary.tasks.navigation.BottomNavActivity
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.common.intent.PendingIntentWrapper
import com.github.naz013.domain.habit.Habit
import com.github.naz013.logic.habit.nudge.HabitNudgeNotifier
import com.github.naz013.logic.notificationaction.NotificationGateway
import com.github.naz013.navigation.DeepLinkDestination
import com.github.naz013.navigation.ViewHabitScreen
import com.github.naz013.ui.common.icon.DrawableCatalog
import com.github.naz013.ui.common.R as UiCommonR

class HabitNudgeNotifierImpl(
  private val contextProvider: ContextProvider,
  private val textProvider: TextProvider,
  private val notificationGateway: NotificationGateway,
) : HabitNudgeNotifier {

  override fun show(habit: Habit) {
    val id = notificationId(habit.id)
    val builder = notificationGateway.builder(NotificationGateway.CHANNEL_REMINDER)
    builder.setAutoCancel(true)
    builder.setContentTitle(habit.name)
    builder.setContentText(textProvider.getText(UiCommonR.string.habit_nudge_text))
    builder.setSmallIcon(DrawableCatalog.Fluent.CheckmarkCircle)
    builder.setCategory(NotificationCompat.CATEGORY_REMINDER)
    builder.setContentIntent(contentIntent(habit.id, id))
    builder.addAction(
      DrawableCatalog.Fluent.Checkmark,
      textProvider.getText(UiCommonR.string.habit_check_in),
      checkInIntent(habit.id, id),
    )
    notificationGateway.notify(id, builder.build())
  }

  override fun cancel(habitId: String) {
    notificationGateway.cancel(notificationId(habitId))
  }

  private fun contentIntent(
    habitId: String,
    requestCode: Int,
  ): PendingIntent =
    PendingIntentWrapper.getActivity(
      contextProvider.context,
      requestCode,
      Intent(contextProvider.context, BottomNavActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .setAction(Intent.ACTION_VIEW)
        .putExtra(DeepLinkDestination.KEY, ViewHabitScreen(habitId) as DeepLinkDestination),
      PendingIntent.FLAG_CANCEL_CURRENT,
    )

  private fun checkInIntent(
    habitId: String,
    requestCode: Int,
  ): PendingIntent =
    PendingIntentWrapper.getBroadcast(
      contextProvider.context,
      requestCode,
      Intent(contextProvider.context, HabitActionReceiver::class.java).apply {
        action = HabitActionReceiver.ACTION_CHECK_IN
        putExtra(IntentKeys.INTENT_ID, habitId)
      },
      PendingIntent.FLAG_CANCEL_CURRENT,
    )

  private fun notificationId(habitId: String): Int = habitId.hashCode()
}
