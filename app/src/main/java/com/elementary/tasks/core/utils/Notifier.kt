package com.elementary.tasks.core.utils

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.SplashScreenActivity
import com.elementary.tasks.core.app_widgets.WidgetUtils
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.PermanentBirthdayReceiver
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.services.ReminderActionReceiver
import com.elementary.tasks.core.utils.PrefsConstants.WEAR_NOTIFICATION
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.reminder.create.CreateReminderActivity
import com.elementary.tasks.reminder.preview.ReminderDialog29Activity
import timber.log.Timber
import java.util.*

class Notifier(private val context: Context, private val prefs: Prefs) {

    init {
        createChannels(context)
    }

    fun showNoteNotification(noteWithImages: NoteWithImages) {
        val note = noteWithImages.note ?: return
        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDER)
        builder.setContentText(context.getString(R.string.note))
        builder.color = ContextCompat.getColor(context, R.color.secondaryBlue)
        val content = note.summary
        builder.setSmallIcon(R.drawable.ic_twotone_note_white)
        builder.setContentTitle(content)
        val isWear = prefs.getBoolean(WEAR_NOTIFICATION)
        if (isWear) {
            builder.setOnlyAlertOnce(true)
            builder.setGroup("GROUP")
            builder.setGroupSummary(true)
        }
        if (noteWithImages.images.isNotEmpty() && Module.isMarshmallow) {
            val image = noteWithImages.images[0]
            val bitmap = BitmapFactory.decodeByteArray(image.image, 0, image.image!!.size)
            builder.setLargeIcon(bitmap)
            val s = NotificationCompat.BigPictureStyle()
            s.bigLargeIcon(bitmap)
            s.bigPicture(bitmap)
            builder.setStyle(s)
        }
        getManager(context)?.notify(note.uniqueId, builder.build())
        if (isWear) {
            val wearableNotificationBuilder = NotificationCompat.Builder(context, CHANNEL_REMINDER)
            wearableNotificationBuilder.setSmallIcon(R.drawable.ic_twotone_note_white)
            wearableNotificationBuilder.setContentTitle(content)
            wearableNotificationBuilder.setContentText(context.getString(R.string.note))
            wearableNotificationBuilder.setOngoing(false)
            wearableNotificationBuilder.color = ContextCompat.getColor(context, R.color.secondaryBlue)
            wearableNotificationBuilder.setOnlyAlertOnce(true)
            wearableNotificationBuilder.setGroup("GROUP")
            wearableNotificationBuilder.setGroupSummary(false)
            getManager(context)?.notify(note.uniqueId, wearableNotificationBuilder.build())
        }
    }

    fun hideNotification(id: Int) {
        getManager(context)?.cancel(id)
    }

    fun updateReminderPermanent(action: String) {
        context.sendBroadcast(Intent(context, PermanentReminderReceiver::class.java)
                .setAction(action))
    }

    fun showBirthdayPermanent() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val list = AppDb.getAppDatabase(context).birthdaysDao().getAll("$day|$month")

        if (list.isNotEmpty()) {
            val dismissIntent = Intent(context, PermanentBirthdayReceiver::class.java)
            dismissIntent.action = PermanentBirthdayReceiver.ACTION_HIDE
            val piDismiss = PendingIntent.getBroadcast(context, 0, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT)

            val builder = NotificationCompat.Builder(context, CHANNEL_REMINDER)
            builder.setSmallIcon(R.drawable.ic_twotone_cake_white)
            builder.setAutoCancel(false)
            builder.setOngoing(true)
            builder.priority = NotificationCompat.PRIORITY_HIGH
            builder.setContentTitle(context.getString(R.string.events))
            val item = list[0]
            builder.setContentText(item.date + " | " + item.name + " | " + TimeUtil.getAgeFormatted(context, item.date, prefs.appLanguage))
            if (list.size > 1) {
                val stringBuilder = StringBuilder()
                for (birthday in list) {
                    stringBuilder.append(birthday.date).append(" | ").append(birthday.name).append(" | ")
                            .append(TimeUtil.getAgeFormatted(context, birthday.date, prefs.appLanguage))
                    stringBuilder.append("\n")
                }
                builder.setStyle(NotificationCompat.BigTextStyle().bigText(stringBuilder.toString()))
            }
            builder.addAction(R.drawable.ic_clear_white_24dp, context.getString(R.string.ok), piDismiss)
            getManager(context)?.notify(PermanentBirthdayReceiver.BIRTHDAY_PERM_ID, builder.build())
        }
    }

    fun showReminderPermanent() {
        Timber.d("showReminderPermanent: ")
        val remoteViews = RemoteViews(context.packageName, R.layout.view_notification)
        val builder = NotificationCompat.Builder(context, CHANNEL_SILENT)
        builder.setAutoCancel(false)
        builder.setSmallIcon(R.drawable.ic_twotone_notifications_white)
        builder.setContent(remoteViews)
        builder.setOngoing(true)
        if (prefs.isSbIconEnabled) {
            builder.priority = NotificationCompat.PRIORITY_MAX
        } else {
            builder.priority = NotificationCompat.PRIORITY_MIN
        }
        val resultIntent = Intent(context, CreateReminderActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(CreateReminderActivity::class.java)
        stackBuilder.addNextIntentWithParentStack(resultIntent)
        val resultPendingIntent = stackBuilder.getPendingIntent(0, 0)
        remoteViews.setOnClickPendingIntent(R.id.notificationAdd, resultPendingIntent)
        val noteIntent = Intent(context, CreateNoteActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val noteBuilder = TaskStackBuilder.create(context)
        noteBuilder.addParentStack(CreateNoteActivity::class.java)
        noteBuilder.addNextIntent(noteIntent)
        val notePendingIntent = noteBuilder.getPendingIntent(0, 0)
        remoteViews.setOnClickPendingIntent(R.id.noteAdd, notePendingIntent)
        val resInt = Intent(context, SplashScreenActivity::class.java)
        val stackInt = TaskStackBuilder.create(context)
        stackInt.addParentStack(SplashScreenActivity::class.java)
        stackInt.addNextIntent(resInt)
        val resultPendingInt = stackInt.getPendingIntent(0, 0)
        remoteViews.setOnClickPendingIntent(R.id.text, resultPendingInt)
        remoteViews.setOnClickPendingIntent(R.id.featured, resultPendingInt)
        val reminders = AppDb.getAppDatabase(context).reminderDao().getAll(active = true, removed = false).toMutableList()
        val count = reminders.size
        for (i in reminders.indices.reversed()) {
            val item = reminders[i]
            val eventTime = item.dateTime
            if (eventTime <= 0) {
                reminders.removeAt(i)
            }
        }
        var event: String? = ""
        var prevTime: Long = 0
        for (i in reminders.indices) {
            val item = reminders[i]
            if (item.dateTime > System.currentTimeMillis()) {
                if (prevTime == 0L) {
                    prevTime = item.dateTime
                    event = item.summary
                } else if (item.dateTime < prevTime) {
                    prevTime = item.dateTime
                    event = item.summary
                }
            }
        }
        if (count != 0) {
            if (!TextUtils.isEmpty(event)) {
                remoteViews.setTextViewText(R.id.text, event)
                remoteViews.setViewVisibility(R.id.featured, View.VISIBLE)
            } else {
                remoteViews.setTextViewText(R.id.text, context.getString(R.string.active_reminders) + " " + count)
                remoteViews.setViewVisibility(R.id.featured, View.GONE)
            }
        } else {
            remoteViews.setTextViewText(R.id.text, context.getString(R.string.no_events))
            remoteViews.setViewVisibility(R.id.featured, View.GONE)
        }
        WidgetUtils.setIcon(remoteViews, R.drawable.ic_twotone_alarm_24px, R.id.notificationAdd)
        WidgetUtils.setIcon(remoteViews, R.drawable.ic_twotone_note_24px, R.id.noteAdd)
        WidgetUtils.setIcon(remoteViews, R.drawable.ic_twotone_notifications_24px, R.id.bellIcon)

        remoteViews.setInt(R.id.notificationBg, "setBackgroundColor", ThemeUtil.getSecondaryColor(context))
        val colorOnSecondary = ThemeUtil.getOnSecondaryColor(context)
        remoteViews.setTextColor(R.id.featured, colorOnSecondary)
        remoteViews.setTextColor(R.id.text, colorOnSecondary)
        getManager(context)?.notify(PermanentReminderReceiver.PERM_ID, builder.build())
    }

    fun showRepeatedNotification(context: Context, reminder: Reminder) {
        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDER)

        if (!SuperUtil.isDoNotDisturbEnabled(context) || SuperUtil.checkNotificationPermission(context) && prefs.isSoundInSilentModeEnabled) {
            val melody = ReminderUtils.getSound(context, prefs, reminder.melodyPath)
            context.grantUriPermission("com.android.systemui", melody.uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            builder.setSound(melody.uri)
        }
        if (prefs.isVibrateEnabled) {
            val pattern: LongArray = if (prefs.isInfiniteVibrateEnabled) {
                longArrayOf(150, 86400000)
            } else {
                longArrayOf(150, 400, 100, 450, 200, 500, 300, 500)
            }
            builder.setVibrate(pattern)
        }
        builder.priority = priority(reminder.priority)
        builder.setContentTitle(reminder.summary)
        builder.setAutoCancel(false)
        if (prefs.isManualRemoveEnabled) {
            builder.setOngoing(false)
        } else {
            builder.setOngoing(true)
        }
        if (Module.isPro && prefs.isLedEnabled) {
            builder.setLights(ledColor(reminder.color), 500, 1000)
        }
        builder.setContentText(appName(context))
        builder.setSmallIcon(R.drawable.ic_twotone_notifications_white)
        builder.color = ThemeUtil.getSecondaryColor(context)
        builder.setCategory(NotificationCompat.CATEGORY_REMINDER)

        if (reminder.priority >= 2 && Module.isQ) {
            val fullScreenIntent = ReminderDialog29Activity.getLaunchIntent(context, reminder.uuId)
            val fullScreenPendingIntent = PendingIntent.getActivity(context, reminder.uniqueId, fullScreenIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            builder.setFullScreenIntent(fullScreenPendingIntent, true)
        } else {
            val notificationIntent = ReminderActionReceiver.showIntent(context, reminder.uuId)
            val intent = PendingIntent.getBroadcast(context, reminder.uniqueId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            builder.setContentIntent(intent)
        }
        val dismissIntent = Intent(context, ReminderActionReceiver::class.java)
        dismissIntent.action = ReminderActionReceiver.ACTION_HIDE
        dismissIntent.putExtra(Constants.INTENT_ID, reminder.uuId)
        val piDismiss = PendingIntent.getBroadcast(context, reminder.uniqueId, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        builder.addAction(R.drawable.ic_twotone_done_white, context.getString(R.string.ok), piDismiss)

        getManager(context)?.notify(reminder.uniqueId, builder.build())
    }

    private fun appName(context: Context): String {
        return if (Module.isPro) {
            context.getString(R.string.app_name_pro)
        } else {
            context.getString(R.string.app_name)
        }
    }

    private fun ledColor(color: Int): Int {
        return if (Module.isPro) {
            if (color != -1) {
                LED.getLED(color)
            } else {
                LED.getLED(prefs.ledColor)
            }
        } else {
            LED.getLED(0)
        }
    }

    private fun priority(priority: Int): Int {
        return when (priority) {
            0 -> NotificationCompat.PRIORITY_MIN
            1 -> NotificationCompat.PRIORITY_LOW
            2 -> NotificationCompat.PRIORITY_DEFAULT
            3 -> NotificationCompat.PRIORITY_HIGH
            else -> NotificationCompat.PRIORITY_MAX
        }
    }

    companion object {
        const val CHANNEL_REMINDER = "reminder.channel.events"
        const val CHANNEL_SILENT = "reminder.channel.silent"
        const val CHANNEL_SYSTEM = "reminder.channel.system"

        private fun createChannels(context: Context) {
            if (Module.isOreo) {
                val manager = context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
                if (manager != null) {
                    manager.createNotificationChannel(createReminderChannel(context))
                    manager.createNotificationChannel(createSystemChannel(context))
                    manager.createNotificationChannel(createSilentChannel(context))
                }
            }
        }

        @TargetApi(Build.VERSION_CODES.O)
        private fun createSystemChannel(context: Context): NotificationChannel {
            val name = context.getString(R.string.info_channel)
            val descr = context.getString(R.string.channel_for_other_info_notifications)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_SYSTEM, name, importance)
            channel.description = descr
            if (Module.isQ) {
                channel.setAllowBubbles(false)
            }
            channel.setShowBadge(false)
            return channel
        }

        @TargetApi(Build.VERSION_CODES.O)
        private fun createReminderChannel(context: Context): NotificationChannel {
            val name = context.getString(R.string.reminder_channel)
            val descr = context.getString(R.string.default_reminder_notifications)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_REMINDER, name, importance)
            channel.description = descr
//            channel.enableLights(true)
//            channel.enableVibration(true)
            if (Module.isQ) {
                channel.setAllowBubbles(false)
            }
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            return channel
        }

        @TargetApi(Build.VERSION_CODES.O)
        private fun createSilentChannel(context: Context): NotificationChannel {
            val name = context.getString(R.string.silent_channel)
            val description = context.getString(R.string.channel_for_silent_notifiations)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_SILENT, name, importance)
            channel.description = description
            channel.enableLights(true)
            channel.enableVibration(false)
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            if (Module.isQ) {
                channel.setAllowBubbles(false)
            }
            return channel
        }

        fun getManager(context: Context): NotificationManager? {
            createChannels(context)
            return context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        }
    }
}
