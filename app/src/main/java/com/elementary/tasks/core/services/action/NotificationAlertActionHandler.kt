package com.elementary.tasks.core.services.action

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.common.intent.PendingIntentWrapper
import com.github.naz013.logging.Logger

/**
 * Template Method base for the "show an ongoing alert notification" handlers (birthdays,
 * reminders, ...). It owns every step that is identical across those domains - building the
 * notification, wiring the dismiss action, grouping for wear, and showing the wear companion
 * notification - and leaves the domain-specific pieces (content, extra actions, receiver) as
 * abstract hooks. The loud/silent presentation differences are supplied via [NotificationStyle]
 * (Decorator) rather than duplicated per subclass, so adding a new domain only means implementing
 * the hooks below, and adding a new presentation style only means implementing [NotificationStyle].
 */
abstract class NotificationAlertActionHandler<T>(
  protected val contextProvider: ContextProvider,
  protected val textProvider: TextProvider,
  private val notifier: Notifier,
  private val prefs: Prefs,
  private val wearNotification: WearNotification,
  private val style: NotificationStyle,
) : ActionHandler<T> {

  override suspend fun handle(data: T) {
    Logger.d(logTag, "handle: style=${style.name}, data=$data")

    val builder = NotificationCompat.Builder(contextProvider.context, channelId(data))
    builder.setAutoCancel(false)
    builder.setOngoing(true)
    builder.setContentTitle(contentTitle(data))
    contentText(data)?.also { builder.setContentText(it) }
    vibrationPattern(data)?.also { builder.setVibrate(it) }
    ledColor(data)?.also { builder.setLights(it, LIGHTS_ON_MS, LIGHTS_OFF_MS) }
    builder.priority = style.resolvePriority(defaultPriority(data))
    builder.setSmallIcon(style.resolveIcon(domainIcon(data)))
    builder.setContentIntent(contentPendingIntent(data))
    style.decorate(builder, contextProvider)

    val dismissPendingIntent = actionPendingIntent(data, dismissActionKey())
    builder.addAction(
      R.drawable.ic_fluent_checkmark,
      textProvider.getText(R.string.ok),
      dismissPendingIntent,
    )
    builder.setDeleteIntent(dismissPendingIntent)
    extraActions(data).forEach { action ->
      builder.addAction(action.icon, action.label, actionPendingIntent(data, action.actionKey))
    }

    val isWear = prefs.isWearEnabled
    if (isWear) {
      builder.setOnlyAlertOnce(true)
      builder.setGroup(groupKey)
      builder.setGroupSummary(true)
    }

    notifier.notify(uniqueId(data), builder.build())
    if (isWear) {
      // Distinct id so this companion post doesn't overwrite (same-id notify replaces) the
      // richer notification above and wipe out its actions/content intent.
      wearNotification.show(-uniqueId(data), contentTitle(data), appName(data), groupKey)
    }
  }

  private fun actionPendingIntent(
    data: T,
    actionKey: String,
  ): PendingIntent {
    val intent =
      Intent(contextProvider.context, receiverClass()).apply {
        action = actionKey
        putExtra(IntentKeys.INTENT_ID, uuId(data))
      }
    return PendingIntentWrapper.getBroadcast(
      contextProvider.context,
      uniqueId(data),
      intent,
      PendingIntent.FLAG_CANCEL_CURRENT,
    )
  }

  override fun toString(): String = "$logTag(style=${style.name})"

  protected abstract val groupKey: String
  protected abstract val logTag: String

  /** Which channel this notification posts to. Defaults to the single static reminder channel;
   *  override to select/create a channel derived from resolved per-notification settings (see
   *  [Notifier.reminderChannelId]) when a field needs to vary per notification on Android 8+,
   *  where a channel's importance/vibration/DND-bypass are locked at creation. */
  protected open fun channelId(data: T): String = Notifier.CHANNEL_REMINDER

  protected abstract fun receiverClass(): Class<out BroadcastReceiver>

  protected abstract fun dismissActionKey(): String

  protected abstract fun extraActions(data: T): List<NotificationAction>

  protected abstract fun uniqueId(data: T): Int

  protected abstract fun uuId(data: T): String

  protected abstract fun contentTitle(data: T): String

  protected abstract fun contentText(data: T): String?

  protected abstract fun domainIcon(data: T): Int

  protected abstract fun defaultPriority(data: T): Int

  protected abstract fun vibrationPattern(data: T): LongArray?

  protected abstract fun ledColor(data: T): Int?

  protected abstract fun appName(data: T): String

  protected abstract fun contentPendingIntent(data: T): PendingIntent

  companion object {
    private const val LIGHTS_ON_MS = 500
    private const val LIGHTS_OFF_MS = 1000
  }
}
