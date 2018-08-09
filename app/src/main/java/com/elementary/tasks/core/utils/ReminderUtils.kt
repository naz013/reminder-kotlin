package com.elementary.tasks.core.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.BirthdayActionService
import com.elementary.tasks.core.services.ReminderActionService
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

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
@Singleton
class ReminderUtils @Inject constructor(private val context: Context, private val prefs: Prefs) {

    private fun getSoundUri(melody: String?): Uri {
        return if (!TextUtils.isEmpty(melody) && !Sound.isDefaultMelody(melody!!)) {
            UriUtil.getUri(context, melody)
        } else {
            val defMelody = prefs.melodyFile
            if (!TextUtils.isEmpty(defMelody) && !Sound.isDefaultMelody(defMelody)) {
                UriUtil.getUri(context, defMelody)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
        }
    }

    fun showSimpleBirthday(id: String) {
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
        if (!SuperUtil.isDoNotDisturbEnabled(context) || SuperUtil.checkNotificationPermission(context)
                && prefs.isSoundInSilentModeEnabled) {
            val melodyPath: String? = if (Module.isPro && !isGlobal()) {
                prefs.birthdayMelody
            } else {
                prefs.melodyFile
            }
            val uri = getSoundUri(melodyPath)
            context.grantUriPermission("com.android.systemui", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            builder.setSound(uri)
        }
        var vibrate = prefs.isVibrateEnabled
        if (Module.isPro && !isGlobal()) {
            vibrate = prefs.isBirthdayVibrationEnabled
        }
        if (vibrate) {
            vibrate = prefs.isInfiniteVibrateEnabled
            if (Module.isPro && !isGlobal()) {
                vibrate = prefs.isBirthdayInfiniteVibrationEnabled
            }
            val pattern: LongArray = if (vibrate) {
                longArrayOf(150, 86400000)
            } else {
                longArrayOf(150, 400, 100, 450, 200, 500, 300, 500)
            }
            builder.setVibrate(pattern)
        }
        if (Module.isPro && prefs.isLedEnabled) {
            var ledColor = LED.getLED(prefs.ledColor)
            if (Module.isPro && !isGlobal()) {
                ledColor = LED.getLED(prefs.birthdayLedColor)
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

    private fun isGlobal(): Boolean {
        return prefs.isBirthdayGlobalEnabled
    }

    fun showSimpleReminder(id: Int) {
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
        val appName: String = if (Module.isPro) {
            context.getString(R.string.app_name_pro)
        } else {
            context.getString(R.string.app_name)
        }
        if (!SuperUtil.isDoNotDisturbEnabled(context) || SuperUtil.checkNotificationPermission(context) && prefs.isSoundInSilentModeEnabled) {
            val uri = getSoundUri(reminder.melodyPath)
            context.grantUriPermission("com.android.systemui", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            builder.setSound(uri)
        }
        if (prefs.isVibrateEnabled) {
            val pattern: LongArray = if (prefs.isInfiniteVibrateEnabled) {
                longArrayOf(150, 86400000)
            } else {
                longArrayOf(150, 400, 100, 450, 200, 500, 300, 500)
            }
            builder.setVibrate(pattern)
        }
        if (Module.isPro && prefs.isLedEnabled) {
            if (reminder.color != -1) {
                builder.setLights(reminder.color, 500, 1000)
            } else {
                builder.setLights(LED.getLED(prefs.ledColor), 500, 1000)
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

    fun getRepeatString(repCode: List<Int>): String {
        val sb = StringBuilder()
        val first = prefs.startDay
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

    fun getTypeString(type: Int): String {
        val res: String
        when {
            Reminder.isKind(type, Reminder.Kind.CALL) -> {
                val init = context.getString(R.string.make_call)
                res = init + " (" + getType(type) + ")"
            }
            Reminder.isKind(type, Reminder.Kind.SMS) -> {
                val init = context.getString(R.string.message)
                res = init + " (" + getType(type) + ")"
            }
            Reminder.isSame(type, Reminder.BY_SKYPE_CALL) -> {
                val init = context.getString(R.string.skype_call)
                res = init + " (" + getType(type) + ")"
            }
            Reminder.isSame(type, Reminder.BY_SKYPE) -> {
                val init = context.getString(R.string.skype_chat)
                res = init + " (" + getType(type) + ")"
            }
            Reminder.isSame(type, Reminder.BY_SKYPE_VIDEO) -> {
                val init = context.getString(R.string.video_call)
                res = init + " (" + getType(type) + ")"
            }
            Reminder.isSame(type, Reminder.BY_DATE_APP) -> {
                val init = context.getString(R.string.application)
                res = init + " (" + getType(type) + ")"
            }
            Reminder.isSame(type, Reminder.BY_DATE_LINK) -> {
                val init = context.getString(R.string.open_link)
                res = init + " (" + getType(type) + ")"
            }
            Reminder.isSame(type, Reminder.BY_DATE_SHOP) -> res = context.getString(R.string.shopping_list)
            Reminder.isSame(type, Reminder.BY_DATE_EMAIL) -> res = context.getString(R.string.e_mail)
            else -> {
                val init = context.getString(R.string.reminder)
                res = init + " (" + getType(type) + ")"
            }
        }
        return res
    }

    fun getType(type: Int): String {
        return when {
            Reminder.isBase(type, Reminder.BY_MONTH) -> context.getString(R.string.day_of_month)
            Reminder.isBase(type, Reminder.BY_WEEK) -> context.getString(R.string.alarm)
            Reminder.isBase(type, Reminder.BY_LOCATION) -> context.getString(R.string.location)
            Reminder.isBase(type, Reminder.BY_OUT) -> context.getString(R.string.place_out)
            Reminder.isSame(type, Reminder.BY_TIME) -> context.getString(R.string.timer)
            Reminder.isBase(type, Reminder.BY_PLACES) -> context.getString(R.string.places)
            Reminder.isBase(type, Reminder.BY_SKYPE) -> context.getString(R.string.skype)
            Reminder.isSame(type, Reminder.BY_DATE_EMAIL) -> context.getString(R.string.e_mail)
            Reminder.isSame(type, Reminder.BY_DATE_SHOP) -> context.getString(R.string.shopping_list)
            Reminder.isBase(type, Reminder.BY_DAY_OF_YEAR) -> context.getString(R.string.yearly)
            else -> context.getString(R.string.by_date)
        }
    }

    companion object {
        const val DAY_CHECKED = 1
        private const val TAG = "ReminderUtils"
    }
}
