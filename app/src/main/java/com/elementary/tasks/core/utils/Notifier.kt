package com.elementary.tasks.core.utils

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.widget.RemoteViews

import com.elementary.tasks.R
import com.elementary.tasks.core.SplashScreen
import com.elementary.tasks.core.appWidgets.WidgetUtils
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.services.PermanentBirthdayReceiver
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.reminder.create_edit.CreateReminderActivity

import java.util.Calendar
import androidx.core.app.NotificationCompat

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

class Notifier(private val mContext: Context) {

    fun showNoteNotification(item: Note) {
        val sPrefs = Prefs.getInstance(mContext)
        val builder = NotificationCompat.Builder(mContext, Notifier.CHANNEL_REMINDER)
        builder.setContentText(mContext.getString(R.string.note))
        if (Module.isLollipop) {
            builder.color = ViewUtils.getColor(mContext, R.color.bluePrimary)
        }
        val content = item.summary
        if (Module.isLollipop) {
            builder.setSmallIcon(R.drawable.ic_note_white)
        } else {
            builder.setSmallIcon(R.drawable.ic_note_nv_white)
        }
        builder.setContentTitle(content)
        val isWear = sPrefs.getBoolean(Prefs.WEAR_NOTIFICATION)
        if (isWear && Module.isJellyMR2) {
            builder.setOnlyAlertOnce(true)
            builder.setGroup("GROUP")
            builder.setGroupSummary(true)
        }
        if (!item.images.isEmpty() && Module.isMarshmallow) {
            val image = item.images[0]
            val bitmap = BitmapFactory.decodeByteArray(image.image, 0, image.image!!.size)
            builder.setLargeIcon(bitmap)
            val s = NotificationCompat.BigPictureStyle()
            s.bigLargeIcon(bitmap)
            s.bigPicture(bitmap)
            builder.setStyle(s)
        }
        val manager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager?.notify(item.uniqueId, builder.build())
        if (isWear && Module.isJellyMR2) {
            val wearableNotificationBuilder = NotificationCompat.Builder(mContext, Notifier.CHANNEL_REMINDER)
            wearableNotificationBuilder.setSmallIcon(R.drawable.ic_note_nv_white)
            wearableNotificationBuilder.setContentTitle(content)
            wearableNotificationBuilder.setContentText(mContext.getString(R.string.note))
            wearableNotificationBuilder.setOngoing(false)
            if (Module.isLollipop) {
                wearableNotificationBuilder.color = ViewUtils.getColor(mContext, R.color.bluePrimary)
            }
            wearableNotificationBuilder.setOnlyAlertOnce(true)
            wearableNotificationBuilder.setGroup("GROUP")
            wearableNotificationBuilder.setGroupSummary(false)
            manager?.notify(item.uniqueId, wearableNotificationBuilder.build())
        }
    }

    companion object {

        val CHANNEL_REMINDER = "reminder.channel1"
        val CHANNEL_SYSTEM = "reminder.channel2"

        private val TAG = "Notifier"

        fun createChannels(context: Context) {
            if (Module.isO) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (manager != null) {
                    manager.createNotificationChannel(createReminderChannel(context))
                    manager.createNotificationChannel(createSystemChannel(context))
                }
            }
        }

        @TargetApi(Build.VERSION_CODES.O)
        private fun createSystemChannel(context: Context): NotificationChannel {
            val name = context.getString(R.string.info_channel)
            val descr = context.getString(R.string.channel_for_other_info_notifications)
            val importance = NotificationManager.IMPORTANCE_LOW
            val mChannel = NotificationChannel(CHANNEL_SYSTEM, name, importance)
            mChannel.description = descr
            mChannel.setShowBadge(false)
            return mChannel
        }

        @TargetApi(Build.VERSION_CODES.O)
        private fun createReminderChannel(context: Context): NotificationChannel {
            val name = context.getString(R.string.reminder_channel)
            val descr = context.getString(R.string.default_reminder_notifications)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(CHANNEL_REMINDER, name, importance)
            mChannel.description = descr
            mChannel.setShowBadge(true)
            return mChannel
        }

        fun hideNotification(context: Context, id: Int) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager?.cancel(id)
        }

        fun updateReminderPermanent(context: Context, action: String) {
            context.sendBroadcast(Intent(context, PermanentReminderReceiver::class.java)
                    .setAction(action))
        }

        fun showBirthdayPermanent(context: Context) {
            val dismissIntent = Intent(context, PermanentBirthdayReceiver::class.java)
            dismissIntent.action = PermanentBirthdayReceiver.ACTION_HIDE
            val piDismiss = PendingIntent.getBroadcast(context, 0, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH)
            val list = AppDb.getAppDatabase(context).birthdaysDao().getAll(day.toString() + "|" + month)
            val builder = NotificationCompat.Builder(context, Notifier.CHANNEL_REMINDER)
            if (Module.isLollipop) {
                builder.setSmallIcon(R.drawable.ic_cake_white_24dp)
            } else {
                builder.setSmallIcon(R.drawable.ic_cake_nv_white)
            }
            builder.setAutoCancel(false)
            builder.setOngoing(true)
            builder.priority = NotificationCompat.PRIORITY_HIGH
            builder.setContentTitle(context.getString(R.string.events))
            if (list.size > 0) {
                val item = list[0]
                builder.setContentText(item.date + " | " + item.name + " | " + TimeUtil.getAgeFormatted(context, item.date))
                if (list.size > 1) {
                    val stringBuilder = StringBuilder()
                    for (birthday in list) {
                        stringBuilder.append(birthday.date).append(" | ").append(birthday.name).append(" | ")
                                .append(TimeUtil.getAgeFormatted(context, birthday.date))
                        stringBuilder.append("\n")
                    }
                    builder.setStyle(NotificationCompat.BigTextStyle().bigText(stringBuilder.toString()))
                }
                if (Module.isLollipop) {
                    builder.addAction(R.drawable.ic_clear_white_24dp, context.getString(R.string.ok), piDismiss)
                } else {
                    builder.addAction(R.drawable.ic_clear_nv_white, context.getString(R.string.ok), piDismiss)
                }
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager?.notify(PermanentBirthdayReceiver.BIRTHDAY_PERM_ID, builder.build())
            }
        }

        fun showReminderPermanent(context: Context) {
            LogUtil.d(TAG, "showPermanent: ")
            val remoteViews = RemoteViews(context.packageName, R.layout.view_notification)
            val builder = NotificationCompat.Builder(context, Notifier.CHANNEL_REMINDER)
            builder.setAutoCancel(false)
            if (Module.isLollipop) {
                builder.setSmallIcon(R.drawable.ic_notifications_white_24dp)
            } else {
                builder.setSmallIcon(R.drawable.ic_notification_nv_white)
            }
            builder.setContent(remoteViews)
            builder.setOngoing(true)
            if (Prefs.getInstance(context).isSbIconEnabled) {
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
            val resInt = Intent(context, SplashScreen::class.java)
            val stackInt = TaskStackBuilder.create(context)
            stackInt.addParentStack(SplashScreen::class.java)
            stackInt.addNextIntent(resInt)
            val resultPendingInt = stackInt.getPendingIntent(0, 0)
            remoteViews.setOnClickPendingIntent(R.id.text, resultPendingInt)
            remoteViews.setOnClickPendingIntent(R.id.featured, resultPendingInt)
            val reminders = AppDb.getAppDatabase(context).reminderDao().getAll(true, false)
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
            val cs = ThemeUtil.getInstance(context)
            WidgetUtils.setIcon(context, remoteViews, R.drawable.ic_alarm_white, R.id.notificationAdd)
            WidgetUtils.setIcon(context, remoteViews, R.drawable.ic_note_white, R.id.noteAdd)
            WidgetUtils.setIcon(context, remoteViews, R.drawable.ic_notifications_white_24dp, R.id.bellIcon)
            remoteViews.setInt(R.id.notificationBg, "setBackgroundColor", cs.getColor(cs.colorPrimary()))
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager?.notify(PermanentReminderReceiver.PERM_ID, builder.build())
        }
    }
}
