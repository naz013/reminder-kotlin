package com.elementary.tasks.core.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.github.naz013.feature.note.UiNoteNotification
import com.elementary.tasks.core.services.PermanentBirthdayReceiver
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.Notifier.Companion.CHANNEL_REMINDER
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.params.PrefsConstants.WEAR_NOTIFICATION
import com.elementary.tasks.navigation.BottomNavActivity
import com.github.naz013.common.Permissions
import com.github.naz013.common.intent.PendingIntentWrapper
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.reminder.v2.NotificationSettings
import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.feature.common.android.SystemServiceProvider
import com.github.naz013.feature.common.coroutine.invokeSuspend
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.DeepLinkDestination
import com.github.naz013.navigation.EditNoteScreen
import com.github.naz013.navigation.EditReminderScreen
import com.github.naz013.notification.NotificationApi
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter
import com.github.naz013.ui.common.theme.ThemeProvider
import org.threeten.bp.LocalDateTime
import java.util.Calendar

class Notifier(
  private val context: Context,
  private val prefs: Prefs,
  private val dateTimeManager: DateTimeManager,
  private val systemServiceProvider: SystemServiceProvider,
  private val reminderV2Repository: ReminderV2Repository,
  private val birthdayRepository: BirthdayRepository,
  private val modelDateTimeFormatter: ModelDateTimeFormatter,
) : NotificationApi {
  fun getNotificationBuilder(channelId: String): NotificationCompat.Builder {
    createChannels()
    return NotificationCompat.Builder(context, channelId)
  }

  override fun createChannels() {
    val manager = systemServiceProvider.provideNotificationManager()
    manager?.run {
      createNotificationChannel(createReminderChannel())
      createNotificationChannel(createSystemChannel())
      createNotificationChannel(createSilentChannel())
      createNotificationChannel(createNoteChannel())
      createNotificationChannel(createCalendarEventChannel())
    }
  }

  private fun createCalendarEventChannel(): NotificationChannel {
    val name = context.getString(R.string.google_calendar_event)
    val descr = context.getString(R.string.channel_for_calendar_event_notifications)
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel = NotificationChannel(CHANNEL_CALENDAR_EVENT, name, importance)
    channel.description = descr
    channel.setAllowBubbles(false)
    channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
    return channel
  }

  private fun createSystemChannel(): NotificationChannel {
    val name = context.getString(R.string.info_channel)
    val descr = context.getString(R.string.channel_for_other_info_notifications)
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel = NotificationChannel(CHANNEL_SYSTEM, name, importance)
    channel.description = descr
    channel.setAllowBubbles(false)
    channel.setShowBadge(false)
    return channel
  }

  private fun createReminderChannel(): NotificationChannel {
    val name = context.getString(R.string.reminder_channel)
    val descr = context.getString(R.string.default_reminder_notifications)
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel = NotificationChannel(CHANNEL_REMINDER, name, importance)
    channel.description = descr
    channel.setAllowBubbles(false)
    channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
    return channel
  }

  /**
   * A `NotificationChannel`'s importance/vibration/DND-bypass are locked at creation on Android
   * 8+, so [CHANNEL_REMINDER] alone can't express per-reminder/per-group overrides for those
   * fields - this derives (and lazily creates) one channel per distinct resolved combination of
   * them instead. `vibrationPattern` only ever takes one of a handful of fixed presets, so the
   * combination space is small and bounded (roughly 50 in the worst case) - no orphan-channel
   * cleanup is needed for that to stay reasonable.
   */
  override fun reminderChannelId(settings: NotificationSettings): String {
    val id = "$CHANNEL_REMINDER.${channelSuffix(settings)}"
    ensureReminderChannel(id, settings)
    return id
  }

  private fun channelSuffix(settings: NotificationSettings): String {
    val key =
      "${settings.vibrate}|${settings.vibrationPattern?.joinToString(",")}|" +
        "${settings.priority.ordinal}|${settings.bypassDoNotDisturb}"
    return Integer.toHexString(key.hashCode())
  }

  private fun ensureReminderChannel(
    id: String,
    settings: NotificationSettings,
  ) {
    val manager = getManager() ?: return
    if (manager.getNotificationChannel(id) != null) return
    val channel =
      NotificationChannel(
        id,
        context.getString(R.string.reminder_channel),
        channelImportance(settings.priority),
      )
    channel.description = channelDescription(settings)
    channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
    channel.setAllowBubbles(false)
    channel.enableVibration(settings.vibrate)
    if (settings.vibrate) {
      settings.vibrationPattern?.toLongArray()?.let { channel.vibrationPattern = it }
    }
    channel.setBypassDnd(settings.bypassDoNotDisturb)
    manager.createNotificationChannel(channel)
  }

  private fun channelImportance(priority: ReminderPriority): Int =
    when (priority) {
      ReminderPriority.LOWEST -> NotificationManager.IMPORTANCE_MIN
      ReminderPriority.LOW -> NotificationManager.IMPORTANCE_LOW
      ReminderPriority.NORMAL -> NotificationManager.IMPORTANCE_DEFAULT
      ReminderPriority.HIGH, ReminderPriority.HIGHEST -> NotificationManager.IMPORTANCE_HIGH
    }

  private fun priorityLabel(priority: ReminderPriority): String =
    context.getString(
      when (priority) {
        ReminderPriority.LOWEST -> R.string.priority_lowest
        ReminderPriority.LOW -> R.string.priority_low
        ReminderPriority.NORMAL -> R.string.priority_normal
        ReminderPriority.HIGH -> R.string.priority_high
        ReminderPriority.HIGHEST -> R.string.priority_highest
      },
    )

  // Multiple dynamically-created channels all show up as separate "Reminders" entries in system
  // Settings > App notifications - this description is what actually distinguishes them there.
  private fun channelDescription(settings: NotificationSettings): String {
    val vibrateLabel = context.getString(if (settings.vibrate) R.string.vibrate else R.string.off)
    val parts =
      listOfNotNull(
        priorityLabel(settings.priority),
        vibrateLabel,
        context.getString(R.string.bypass_do_not_disturb).takeIf { settings.bypassDoNotDisturb },
      )
    return "${context.getString(R.string.default_reminder_notifications)} (${parts.joinToString(", ")})"
  }

  private fun createNoteChannel(): NotificationChannel {
    val name = context.getString(R.string.note_channel)
    val descr = context.getString(R.string.default_note_notifications)
    val importance = NotificationManager.IMPORTANCE_LOW
    val channel = NotificationChannel(CHANNEL_NOTES, name, importance)
    channel.description = descr
    channel.setAllowBubbles(false)
    channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
    return channel
  }

  private fun createSilentChannel(): NotificationChannel {
    val name = context.getString(R.string.silent_channel)
    val description = context.getString(R.string.channel_for_silent_notifiations)
    val importance = NotificationManager.IMPORTANCE_LOW
    val channel = NotificationChannel(CHANNEL_SILENT, name, importance)
    channel.description = description
    channel.enableLights(true)
    channel.enableVibration(false)
    channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
    channel.setAllowBubbles(false)
    return channel
  }

  fun notify(
    id: Int,
    notification: Notification,
  ) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (Permissions.isNotificationsAllowed(context)) {
        getManager()?.notify(id, notification)
      } else {
        Logger.i(TAG, "Notification not allowed by user")
      }
    } else {
      getManager()?.notify(id, notification)
    }
  }

  private fun getManager(): NotificationManager? {
    createChannels()
    return systemServiceProvider.provideNotificationManager()
  }

  // Checked for Notification permission
  fun showNoteNotification(uiNoteNotification: UiNoteNotification) {
    val builder = NotificationCompat.Builder(context, CHANNEL_NOTES)
    builder.setContentText(context.getString(R.string.note))
    builder.color = ContextCompat.getColor(context, R.color.secondaryBlue)
    builder.setSmallIcon(R.drawable.ic_fluent_note)
    builder.setContentTitle(uiNoteNotification.text)
    val isWear = prefs.getBoolean(WEAR_NOTIFICATION)
    if (isWear) {
      builder.setOnlyAlertOnce(true)
      builder.setGroup("GROUP")
      builder.setGroupSummary(true)
    }
    val image = uiNoteNotification.image
    if (image != null) {
      builder.setLargeIcon(image)
      val s = NotificationCompat.BigPictureStyle()
      s.bigLargeIcon(image)
      s.bigPicture(image)
      builder.setStyle(s)
    }
    notify(uiNoteNotification.uniqueId, builder.build())
    if (isWear) {
      val wearableNotificationBuilder = NotificationCompat.Builder(context, CHANNEL_REMINDER)
      wearableNotificationBuilder.setSmallIcon(R.drawable.ic_fluent_note)
      wearableNotificationBuilder.setContentTitle(uiNoteNotification.text)
      wearableNotificationBuilder.setContentText(context.getString(R.string.note))
      wearableNotificationBuilder.setOngoing(false)
      wearableNotificationBuilder.color = ContextCompat.getColor(context, R.color.secondaryBlue)
      wearableNotificationBuilder.setOnlyAlertOnce(true)
      wearableNotificationBuilder.setGroup("GROUP")
      wearableNotificationBuilder.setGroupSummary(false)
      notify(uiNoteNotification.uniqueId, wearableNotificationBuilder.build())
    }
  }

  override fun sendShowReminderPermanent() {
    PermanentReminderReceiver.show(context)
  }

  override fun cancel(id: Int) {
    getManager()?.cancel(id)
  }

  // Checked for Notification permission
  override fun showReminderPermanent() {
    Logger.d(TAG, "showReminderPermanent: ")
    val remoteViews = RemoteViews(context.packageName, R.layout.view_notification)
    val builder = NotificationCompat.Builder(context, CHANNEL_SILENT)
    builder.setAutoCancel(false)
    builder.setSmallIcon(R.drawable.ic_fluent_alert)
    builder.setContent(remoteViews)
    builder.setOngoing(true)
    if (prefs.isSbIconEnabled) {
      builder.priority = NotificationCompat.PRIORITY_MAX
    } else {
      builder.priority = NotificationCompat.PRIORITY_MIN
    }
    val resultIntent =
      Intent(context, BottomNavActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .setAction(Intent.ACTION_VIEW)
        .putExtra(DeepLinkDestination.KEY, EditReminderScreen())
    val stackBuilder = TaskStackBuilder.create(context)
    stackBuilder.addParentStack(BottomNavActivity::class.java)
    stackBuilder.addNextIntentWithParentStack(resultIntent)
    val resultPendingIntent =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
      } else {
        stackBuilder.getPendingIntent(0, 0)
      }
    remoteViews.setOnClickPendingIntent(R.id.notificationAdd, resultPendingIntent)
    val noteIntent =
      Intent(context, BottomNavActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .setAction(Intent.ACTION_VIEW)
        .putExtra(DeepLinkDestination.KEY, EditNoteScreen())
    val noteBuilder = TaskStackBuilder.create(context)
    noteBuilder.addParentStack(BottomNavActivity::class.java)
    noteBuilder.addNextIntentWithParentStack(noteIntent)
    val notePendingIntent =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        noteBuilder.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
      } else {
        noteBuilder.getPendingIntent(0, 0)
      }
    remoteViews.setOnClickPendingIntent(R.id.noteAdd, notePendingIntent)
    val resInt = Intent(context, BottomNavActivity::class.java)
    val stackInt = TaskStackBuilder.create(context)
    stackInt.addParentStack(BottomNavActivity::class.java)
    stackInt.addNextIntent(resInt)
    val resultPendingInt =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        stackInt.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
      } else {
        stackInt.getPendingIntent(0, 0)
      }
    remoteViews.setOnClickPendingIntent(R.id.text, resultPendingInt)
    remoteViews.setOnClickPendingIntent(R.id.featured, resultPendingInt)
    val reminders = invokeSuspend { reminderV2Repository.getAll(active = true, removed = false) }.toMutableList()
    val count = reminders.size

    for (i in reminders.indices.reversed()) {
      val eventTime = reminders[i].schedule.eventDateTime
      if (eventTime == null) {
        reminders.removeAt(i)
      }
    }
    var event: String? = ""
    var prevTime: LocalDateTime? = null
    reminders.forEach { reminder ->
      val dateTime = reminder.schedule.eventDateTime?.let { dateTimeManager.utcToLocal(it) }
      if (dateTime != null && dateTimeManager.isCurrent(dateTime)) {
        if (prevTime == null) {
          prevTime = dateTime
          event = reminder.summary
        } else if (dateTime < prevTime) {
          prevTime = dateTime
          event = reminder.summary
        }
      }
    }
    if (count != 0) {
      if (!TextUtils.isEmpty(event)) {
        remoteViews.setTextViewText(R.id.text, event)
        remoteViews.setViewVisibility(R.id.featured, View.VISIBLE)
      } else {
        remoteViews.setTextViewText(
          R.id.text,
          context.getString(R.string.active_reminders) + " " + count,
        )
        remoteViews.setViewVisibility(R.id.featured, View.GONE)
      }
    } else {
      remoteViews.setTextViewText(R.id.text, context.getString(R.string.no_events))
      remoteViews.setViewVisibility(R.id.featured, View.GONE)
    }
    setIcon(remoteViews, R.drawable.ic_fluent_clock_alarm, R.id.notificationAdd)
    setIcon(remoteViews, R.drawable.ic_fluent_note, R.id.noteAdd)
    setIcon(remoteViews, R.drawable.ic_fluent_alert, R.id.bellIcon)

    remoteViews.setInt(
      R.id.notificationBg,
      "setBackgroundColor",
      ThemeProvider.getPrimaryColor(context),
    )
    val colorOnPrimary = ThemeProvider.getOnPrimaryColor(context)
    remoteViews.setTextColor(R.id.featured, colorOnPrimary)
    remoteViews.setTextColor(R.id.text, colorOnPrimary)
    notify(PermanentReminderReceiver.PERM_ID, builder.build())
  }

  private fun setIcon(
    rv: RemoteViews,
    @DrawableRes iconId: Int,
    @IdRes viewId: Int,
  ) {
    rv.setImageViewResource(viewId, iconId)
  }

  override fun showBirthdayPermanent() {
    if (!prefs.isBirthdayPermanentEnabled) {
      return
    }
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = System.currentTimeMillis()
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val month = calendar.get(Calendar.MONTH)
    val list = invokeSuspend { birthdayRepository.getAll("$day|$month") }

    if (list.isNotEmpty()) {
      val dismissIntent = Intent(context, PermanentBirthdayReceiver::class.java)
      dismissIntent.action = PermanentBirthdayReceiver.ACTION_HIDE
      val piDismiss =
        PendingIntentWrapper.getBroadcast(context, 0, dismissIntent, PendingIntent.FLAG_IMMUTABLE)

      val builder = NotificationCompat.Builder(context, CHANNEL_REMINDER)
      builder.setSmallIcon(R.drawable.ic_fluent_food_cake)
      builder.setAutoCancel(false)
      builder.setOngoing(true)
      builder.priority = NotificationCompat.PRIORITY_HIGH
      builder.setContentTitle(context.getString(R.string.events))
      val item = list[0]
      builder.setContentText(formatSummary(item))
      if (list.size > 1) {
        val stringBuilder = StringBuilder()
        for (birthday in list) {
          stringBuilder.append(formatSummary(birthday))
          stringBuilder.append("\n")
        }
        builder.setStyle(NotificationCompat.BigTextStyle().bigText(stringBuilder.toString()))
      }
      builder.addAction(R.drawable.ic_fluent_dismiss, context.getString(R.string.ok), piDismiss)
      notify(PermanentBirthdayReceiver.BIRTHDAY_PERM_ID, builder.build())
    }
  }

  private fun formatSummary(birthday: Birthday): String {
    val date =
      dateTimeManager.parseBirthdayDate(birthday.date)?.let {
        dateTimeManager.formatBirthdayDateForUi(it, birthday.ignoreYear)
      }
    return if (birthday.ignoreYear) {
      date + " | " + birthday.name
    } else {
      date + " | " + birthday.name + " | " + modelDateTimeFormatter.getAgeFormatted(birthday.date)
    }
  }

  companion object {
    private const val TAG = "Notifier"
    const val CHANNEL_REMINDER = "reminder.channel.events"
    const val CHANNEL_NOTES = "reminder.channel.notes"
    const val CHANNEL_SILENT = "reminder.channel.silent"
    const val CHANNEL_SYSTEM = "reminder.channel.system"
    const val CHANNEL_CALENDAR_EVENT = "reminder.channel.calendar_event"
  }
}
