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
import com.elementary.tasks.core.services.PermanentBirthdayReceiver
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.PrefsConstants.WEAR_NOTIFICATION
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.reminder.create.CreateReminderActivity
import timber.log.Timber
import java.util.*

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class Notifier(private val context: Context, private val prefs: Prefs, private val themeUtil: ThemeUtil) {

    init {
        createChannels(context)
    }

    fun showNoteNotification(noteWithImages: NoteWithImages) {
        val note = noteWithImages.note ?: return
        val builder = NotificationCompat.Builder(context, Notifier.CHANNEL_REMINDER)
        builder.setContentText(context.getString(R.string.note))
        builder.color = ContextCompat.getColor(context, R.color.bluePrimary)
        val content = note.summary
        builder.setSmallIcon(R.drawable.ic_twotone_note_white)
        builder.setContentTitle(content)
        val isWear = prefs.getBoolean(WEAR_NOTIFICATION)
        if (isWear) {
            builder.setOnlyAlertOnce(true)
            builder.setGroup("GROUP")
            builder.setGroupSummary(true)
        }
        if (!noteWithImages.images.isEmpty() && Module.isMarshmallow) {
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
            val wearableNotificationBuilder = NotificationCompat.Builder(context, Notifier.CHANNEL_REMINDER)
            wearableNotificationBuilder.setSmallIcon(R.drawable.ic_twotone_note_white)
            wearableNotificationBuilder.setContentTitle(content)
            wearableNotificationBuilder.setContentText(context.getString(R.string.note))
            wearableNotificationBuilder.setOngoing(false)
            wearableNotificationBuilder.color = ContextCompat.getColor(context, R.color.bluePrimary)
            wearableNotificationBuilder.setOnlyAlertOnce(true)
            wearableNotificationBuilder.setGroup("GROUP")
            wearableNotificationBuilder.setGroupSummary(false)
            getManager(context)?.notify(note.uniqueId, wearableNotificationBuilder.build())
        }
    }

    private fun createChannels(context: Context) {
        if (Module.isOreo) {
            val manager = getManager(context)
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
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_SYSTEM, name, importance)
        channel.description = descr
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
        channel.enableLights(true)
        channel.enableVibration(true)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
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
        return channel
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

            val builder = NotificationCompat.Builder(context, Notifier.CHANNEL_REMINDER)
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
        val builder = NotificationCompat.Builder(context, Notifier.CHANNEL_SILENT)
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
        if (prefs.appThemeColor == ThemeUtil.Color.BLACK) {
            WidgetUtils.setIcon(remoteViews, R.drawable.ic_twotone_alarm_white, R.id.notificationAdd)
            WidgetUtils.setIcon(remoteViews, R.drawable.ic_twotone_note_white, R.id.noteAdd)
            WidgetUtils.setIcon(remoteViews, R.drawable.ic_twotone_notifications_white, R.id.bellIcon)
        } else {
            WidgetUtils.setIcon(remoteViews, R.drawable.ic_twotone_alarm_24px, R.id.notificationAdd)
            WidgetUtils.setIcon(remoteViews, R.drawable.ic_twotone_note_24px, R.id.noteAdd)
            WidgetUtils.setIcon(remoteViews, R.drawable.ic_twotone_notifications_24px, R.id.bellIcon)
        }

        remoteViews.setInt(R.id.notificationBg, "setBackgroundColor", themeUtil.getSecondaryColor())
        val colorOnSecondary = themeUtil.getOnSecondaryColor()
        remoteViews.setTextColor(R.id.featured, colorOnSecondary)
        remoteViews.setTextColor(R.id.text, colorOnSecondary)
        getManager(context)?.notify(PermanentReminderReceiver.PERM_ID, builder.build())
    }

    companion object {
        const val CHANNEL_REMINDER = "reminder.channel1"
        const val CHANNEL_SILENT = "reminder.channel3"
        const val CHANNEL_SYSTEM = "reminder.channel2"

        fun getManager(context: Context): NotificationManager? {
            return context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        }
    }
}
