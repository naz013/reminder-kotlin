package com.elementary.tasks.core.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.text.TextUtils

import com.elementary.tasks.R
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.BirthdayActionService
import com.elementary.tasks.core.services.ReminderActionService

import java.util.Calendar

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

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
object ReminderUtils {

    internal val DAY_CHECKED = 1
    private val TAG = "ReminderUtils"

    private fun getSoundUri(melody: String?, context: Context): Uri {
        if (!TextUtils.isEmpty(melody) && !Sound.isDefaultMelody(melody!!)) {
            return UriUtil.getUri(context, melody)
        } else {
            val defMelody = Prefs.getInstance(context).melodyFile
            return if (!TextUtils.isEmpty(defMelody) && !Sound.isDefaultMelody(defMelody!!)) {
                UriUtil.getUri(context, defMelody)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
        }
    }

    fun showSimpleBirthday(context: Context, id: Int) {
        val birthday = AppDb.getAppDatabase(context).birthdaysDao().getById(id) ?: return
        val builder = NotificationCompat.Builder(context, Notifier.CHANNEL_REMINDER)
        if (Module.isLollipop) {
            builder.setSmallIcon(R.drawable.ic_cake_white_24dp)
        } else {
            builder.setSmallIcon(R.drawable.ic_cake_nv_white)
        }
        val intent = PendingIntent.getBroadcast(context, birthday.uniqueId,
                BirthdayActionService.show(context, id), PendingIntent.FLAG_CANCEL_CURRENT)
        builder.setContentIntent(intent)
        builder.setAutoCancel(false)
        builder.setOngoing(true)
        builder.priority = NotificationCompat.PRIORITY_HIGH
        builder.setContentTitle(birthday.name)
        if (!SuperUtil.isDoNotDisturbEnabled(context) || SuperUtil.checkNotificationPermission(context) && Prefs.getInstance(context).isSoundInSilentModeEnabled) {
            val melodyPath: String?
            if (Module.isPro && !isGlobal(context)) {
                melodyPath = Prefs.getInstance(context).birthdayMelody
            } else {
                melodyPath = Prefs.getInstance(context).melodyFile
            }
            val uri = getSoundUri(melodyPath, context)
            context.grantUriPermission("com.android.systemui", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            builder.setSound(uri)
        }
        var vibrate = Prefs.getInstance(context).isVibrateEnabled
        if (Module.isPro && !isGlobal(context)) {
            vibrate = Prefs.getInstance(context).isBirthdayVibrationEnabled
        }
        if (vibrate) {
            vibrate = Prefs.getInstance(context).isInfiniteVibrateEnabled
            if (Module.isPro && !isGlobal(context)) {
                vibrate = Prefs.getInstance(context).isBirthdayInfiniteVibrationEnabled
            }
            val pattern: LongArray
            if (vibrate) {
                pattern = longArrayOf(150, 86400000)
            } else {
                pattern = longArrayOf(150, 400, 100, 450, 200, 500, 300, 500)
            }
            builder.setVibrate(pattern)
        }
        if (Module.isPro && Prefs.getInstance(context).isLedEnabled) {
            var ledColor = LED.getLED(Prefs.getInstance(context).ledColor)
            if (Module.isPro && !isGlobal(context)) {
                ledColor = LED.getLED(Prefs.getInstance(context).birthdayLedColor)
            }
            builder.setLights(ledColor, 500, 1000)
        }
        builder.setContentText(context.getString(R.string.birthday))

        val piDismiss = PendingIntent.getBroadcast(context, birthday.uniqueId,
                BirthdayActionService.hide(context, id), PendingIntent.FLAG_CANCEL_CURRENT)
        if (Module.isLollipop) {
            builder.addAction(R.drawable.ic_done_white_24dp, context.getString(R.string.ok), piDismiss)
        } else {
            builder.addAction(R.drawable.ic_done_nv_white, context.getString(R.string.ok), piDismiss)
        }

        if (!TextUtils.isEmpty(birthday.number)) {
            val piCall = PendingIntent.getBroadcast(context, birthday.uniqueId,
                    BirthdayActionService.call(context, id), PendingIntent.FLAG_CANCEL_CURRENT)
            if (Module.isLollipop) {
                builder.addAction(R.drawable.ic_call_white_24dp, context.getString(R.string.make_call), piCall)
            } else {
                builder.addAction(R.drawable.ic_call_nv_white, context.getString(R.string.make_call), piCall)
            }

            val piSms = PendingIntent.getBroadcast(context, birthday.uniqueId,
                    BirthdayActionService.sms(context, id), PendingIntent.FLAG_CANCEL_CURRENT)
            if (Module.isLollipop) {
                builder.addAction(R.drawable.ic_send_white_24dp, context.getString(R.string.send_sms), piSms)
            } else {
                builder.addAction(R.drawable.ic_send_nv_white, context.getString(R.string.send_sms), piSms)
            }
        }

        val mNotifyMgr = NotificationManagerCompat.from(context)
        mNotifyMgr.notify(birthday.uniqueId, builder.build())
    }

    private fun isGlobal(context: Context): Boolean {
        return Prefs.getInstance(context).isBirthdayGlobalEnabled
    }

    fun showSimpleReminder(context: Context, id: Int) {
        LogUtil.d(TAG, "showSimpleReminder: ")
        val reminder = AppDb.getAppDatabase(context).reminderDao().getById(id) ?: return
        val dismissIntent = Intent(context, ReminderActionService::class.java)
        dismissIntent.action = ReminderActionService.ACTION_HIDE
        dismissIntent.putExtra(Constants.INTENT_ID, id)
        val piDismiss = PendingIntent.getBroadcast(context, reminder.uniqueId, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        val builder = NotificationCompat.Builder(context, Notifier.CHANNEL_REMINDER)
        if (Module.isLollipop) {
            builder.setSmallIcon(R.drawable.ic_notifications_white_24dp)
        } else {
            builder.setSmallIcon(R.drawable.ic_notification_nv_white)
        }
        val notificationIntent = Intent(context, ReminderActionService::class.java)
        notificationIntent.action = ReminderActionService.ACTION_SHOW
        notificationIntent.putExtra(Constants.INTENT_ID, id)
        val intent = PendingIntent.getBroadcast(context, reminder.uniqueId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        builder.setContentIntent(intent)
        builder.setAutoCancel(false)
        builder.setOngoing(true)
        builder.priority = NotificationCompat.PRIORITY_HIGH
        builder.setContentTitle(reminder.summary)
        val appName: String
        if (Module.isPro) {
            appName = context.getString(R.string.app_name_pro)
        } else {
            appName = context.getString(R.string.app_name)
        }
        if (!SuperUtil.isDoNotDisturbEnabled(context) || SuperUtil.checkNotificationPermission(context) && Prefs.getInstance(context).isSoundInSilentModeEnabled) {
            val uri = getSoundUri(reminder.melodyPath, context)
            context.grantUriPermission("com.android.systemui", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            builder.setSound(uri)
        }
        if (Prefs.getInstance(context).isVibrateEnabled) {
            val pattern: LongArray
            if (Prefs.getInstance(context).isInfiniteVibrateEnabled) {
                pattern = longArrayOf(150, 86400000)
            } else {
                pattern = longArrayOf(150, 400, 100, 450, 200, 500, 300, 500)
            }
            builder.setVibrate(pattern)
        }
        if (Module.isPro && Prefs.getInstance(context).isLedEnabled) {
            if (reminder.color != -1) {
                builder.setLights(reminder.color, 500, 1000)
            } else {
                builder.setLights(LED.getLED(Prefs.getInstance(context).ledColor), 500, 1000)
            }
        }
        builder.setContentText(appName)
        if (Module.isLollipop) {
            builder.addAction(R.drawable.ic_done_white_24dp, context.getString(R.string.ok), piDismiss)
        } else {
            builder.addAction(R.drawable.ic_done_nv_white, context.getString(R.string.ok), piDismiss)
        }
        val mNotifyMgr = NotificationManagerCompat.from(context)
        mNotifyMgr.notify(reminder.uniqueId, builder.build())
    }

    fun getTime(day: Int, month: Int, year: Int, hour: Int, minute: Int, after: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, hour, minute, 0)
        return calendar.timeInMillis + after
    }

    fun getRepeatString(context: Context, repCode: List<Int>): String {
        val sb = StringBuilder()
        val first = Prefs.getInstance(context).startDay
        if (first == 0 && repCode[0] == DAY_CHECKED) {
            sb.append(" ")
            sb.append(context.getString(R.string.sun))
        }
        if (repCode[1] == DAY_CHECKED) {
            sb.append(" ")
            sb.append(context.getString(R.string.mon))
        }
        if (repCode[2] == DAY_CHECKED) {
            sb.append(" ")
            sb.append(context.getString(R.string.tue))
        }
        if (repCode[3] == DAY_CHECKED) {
            sb.append(" ")
            sb.append(context.getString(R.string.wed))
        }
        if (repCode[4] == DAY_CHECKED) {
            sb.append(" ")
            sb.append(context.getString(R.string.thu))
        }
        if (repCode[5] == DAY_CHECKED) {
            sb.append(" ")
            sb.append(context.getString(R.string.fri))
        }
        if (repCode[6] == DAY_CHECKED) {
            sb.append(" ")
            sb.append(context.getString(R.string.sat))
        }
        if (first == 1 && repCode[0] == DAY_CHECKED) {
            sb.append(" ")
            sb.append(context.getString(R.string.sun))
        }
        return if (isAllChecked(repCode)) {
            context.getString(R.string.everyday)
        } else {
            sb.toString()
        }
    }

    private fun isAllChecked(repCode: List<Int>): Boolean {
        var `is` = true
        for (i in repCode) {
            if (i == 0) {
                `is` = false
                break
            }
        }
        return `is`
    }

    fun getTypeString(context: Context, type: Int): String {
        val res: String
        if (Reminder.isKind(type, Reminder.Kind.CALL)) {
            val init = context.getString(R.string.make_call)
            res = init + " (" + getType(context, type) + ")"
        } else if (Reminder.isKind(type, Reminder.Kind.SMS)) {
            val init = context.getString(R.string.message)
            res = init + " (" + getType(context, type) + ")"
        } else if (Reminder.isSame(type, Reminder.BY_SKYPE_CALL)) {
            val init = context.getString(R.string.skype_call)
            res = init + " (" + getType(context, type) + ")"
        } else if (Reminder.isSame(type, Reminder.BY_SKYPE)) {
            val init = context.getString(R.string.skype_chat)
            res = init + " (" + getType(context, type) + ")"
        } else if (Reminder.isSame(type, Reminder.BY_SKYPE_VIDEO)) {
            val init = context.getString(R.string.video_call)
            res = init + " (" + getType(context, type) + ")"
        } else if (Reminder.isSame(type, Reminder.BY_DATE_APP)) {
            val init = context.getString(R.string.application)
            res = init + " (" + getType(context, type) + ")"
        } else if (Reminder.isSame(type, Reminder.BY_DATE_LINK)) {
            val init = context.getString(R.string.open_link)
            res = init + " (" + getType(context, type) + ")"
        } else if (Reminder.isSame(type, Reminder.BY_DATE_SHOP)) {
            res = context.getString(R.string.shopping_list)
        } else if (Reminder.isSame(type, Reminder.BY_DATE_EMAIL)) {
            res = context.getString(R.string.e_mail)
        } else {
            val init = context.getString(R.string.reminder)
            res = init + " (" + getType(context, type) + ")"
        }
        return res
    }

    fun getType(context: Context, type: Int): String {
        val res: String
        if (Reminder.isBase(type, Reminder.BY_MONTH)) {
            res = context.getString(R.string.day_of_month)
        } else if (Reminder.isBase(type, Reminder.BY_WEEK)) {
            res = context.getString(R.string.alarm)
        } else if (Reminder.isBase(type, Reminder.BY_LOCATION)) {
            res = context.getString(R.string.location)
        } else if (Reminder.isBase(type, Reminder.BY_OUT)) {
            res = context.getString(R.string.place_out)
        } else if (Reminder.isSame(type, Reminder.BY_TIME)) {
            res = context.getString(R.string.timer)
        } else if (Reminder.isBase(type, Reminder.BY_PLACES)) {
            res = context.getString(R.string.places)
        } else if (Reminder.isBase(type, Reminder.BY_SKYPE)) {
            res = context.getString(R.string.skype)
        } else if (Reminder.isSame(type, Reminder.BY_DATE_EMAIL)) {
            res = context.getString(R.string.e_mail)
        } else if (Reminder.isSame(type, Reminder.BY_DATE_SHOP)) {
            res = context.getString(R.string.shopping_list)
        } else if (Reminder.isBase(type, Reminder.BY_DAY_OF_YEAR)) {
            res = context.getString(R.string.yearly)
        } else {
            res = context.getString(R.string.by_date)
        }
        return res
    }
}
