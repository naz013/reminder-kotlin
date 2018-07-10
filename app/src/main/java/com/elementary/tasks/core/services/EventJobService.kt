package com.elementary.tasks.core.services

import android.app.AlarmManager
import android.content.Context
import android.content.Intent

import com.elementary.tasks.birthdays.ShowBirthdayActivity
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ReminderUtils
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.missed_calls.MissedCallDialogActivity
import com.evernote.android.job.Job
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat

import java.util.Calendar
import timber.log.Timber

import com.elementary.tasks.core.utils.TimeUtil.birthFormat

/**
 * Copyright 2018 Nazar Suhovich
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
class EventJobService : Job() {

    override fun onRunJob(params: Job.Params): Job.Result {
        Timber.d("onRunJob: %s, tag -> %s", TimeUtil.getGmtFromDateTime(System.currentTimeMillis()), params.tag)
        when (params.tag) {
            EVENT_BIRTHDAY -> birthdayAction(context)
            else -> {
                val bundle = params.extras
                if (bundle.getBoolean(ARG_MISSED, false)) {
                    openMissedScreen(params.tag)
                    enableMissedCall(context, params.tag)
                } else if (bundle.getBoolean(ARG_LOCATION, false)) {
                    SuperUtil.startGpsTracking(context)
                } else {
                    try {
                        start(context, Integer.parseInt(params.tag))
                    } catch (ignored: NumberFormatException) {
                    }

                }
            }
        }
        return Job.Result.SUCCESS
    }

    private fun birthdayAction(context: Context) {
        cancelBirthdayAlarm()
        enableBirthdayAlarm(context)
        Thread {
            val daysBefore = Prefs.getInstance(context).daysToBirthday
            val cal = Calendar.getInstance()
            cal.timeInMillis = System.currentTimeMillis()
            val mYear = cal.get(Calendar.YEAR)
            val mDate = birthFormat.format(cal.time)
            for (item in AppDb.getAppDatabase(context).birthdaysDao().all) {
                val year = item.showedYear
                val birthValue = getBirthdayValue(item.month, item.day, daysBefore)
                if (birthValue == mDate && year != mYear) {
                    showBirthday(context, item)
                }
            }
        }.start()
    }

    private fun getBirthdayValue(month: Int, day: Int, daysBefore: Int): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.timeInMillis = calendar.timeInMillis - AlarmManager.INTERVAL_DAY * daysBefore
        return birthFormat.format(calendar.time)
    }

    private fun showBirthday(context: Context, item: Birthday) {
        if (Prefs.getInstance(context).reminderType == 0) {
            context.startActivity(ShowBirthdayActivity.getLaunchIntent(context, item.uniqueId))
        } else {
            ReminderUtils.showSimpleBirthday(context, item.uniqueId)
        }
    }

    private fun openMissedScreen(tag: String) {
        val resultIntent = Intent(context, MissedCallDialogActivity::class.java)
        resultIntent.putExtra(Constants.INTENT_ID, tag)
        resultIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        context.startActivity(resultIntent)
    }

    private fun start(context: Context, id: Int) {
        val intent = Intent(context, ReminderActionService::class.java)
        intent.action = ReminderActionService.ACTION_RUN
        intent.putExtra(Constants.INTENT_ID, id)
        context.sendBroadcast(intent)
    }

    companion object {

        private val TAG = "EventJobService"
        private val EVENT_BIRTHDAY = "event_birthday"
        private val EVENT_BIRTHDAY_CHECK = "event_birthday_check"
        private val EVENT_BIRTHDAY_PERMANENT = "event_birthday_permanent"
        private val EVENT_CHECK = "event_check"
        private val EVENT_SYNC = "event_sync"

        private val ARG_LOCATION = "arg_location"
        private val ARG_MISSED = "arg_missed"
        private val ARG_REPEAT = "arg_repeat"

        fun cancelBirthdayAlarm() {
            cancelReminder(EVENT_BIRTHDAY)
        }

        fun enableBirthdayAlarm(context: Context) {
            val time = Prefs.getInstance(context).birthdayTime
            val mills = TimeUtil.getBirthdayTime(time) - System.currentTimeMillis()
            if (mills <= 0) return
            JobRequest.Builder(EVENT_BIRTHDAY)
                    .setExact(mills)
                    .setRequiresCharging(false)
                    .setRequiresDeviceIdle(false)
                    .setRequiresBatteryNotLow(false)
                    .build()
                    .schedule()
        }

        internal fun enableMissedCall(context: Context, number: String?) {
            if (number == null) return
            val time = Prefs.getInstance(context).missedReminderTime
            val mills = (time * (1000 * 60)).toLong()
            val bundle = PersistableBundleCompat()
            bundle.putBoolean(ARG_MISSED, true)
            JobRequest.Builder(number)
                    .setExact(mills)
                    .setRequiresCharging(false)
                    .setRequiresDeviceIdle(false)
                    .setRequiresBatteryNotLow(false)
                    .setRequiresStorageNotLow(false)
                    .setExtras(bundle)
                    .setUpdateCurrent(true)
                    .build()
                    .schedule()
        }

        fun cancelMissedCall(number: String?) {
            if (number == null) return
            cancelReminder(number)
        }

        fun enableDelay(time: Int, id: Int) {
            val min = TimeCount.MINUTE
            val due = System.currentTimeMillis() + min * time
            val mills = due - System.currentTimeMillis()
            if (due == 0L || mills <= 0) {
                return
            }
            JobRequest.Builder(id.toString())
                    .setExact(mills)
                    .setRequiresCharging(false)
                    .setRequiresDeviceIdle(false)
                    .setRequiresBatteryNotLow(false)
                    .setRequiresStorageNotLow(false)
                    .setUpdateCurrent(true)
                    .build()
                    .schedule()
        }

        fun enablePositionDelay(context: Context, id: Int): Boolean {
            val item = AppDb.getAppDatabase(context).reminderDao().getById(id) ?: return false
            val due = TimeUtil.getDateTimeFromGmt(item.eventTime)
            val mills = due - System.currentTimeMillis()
            if (due == 0L || mills <= 0) {
                return false
            }
            val bundle = PersistableBundleCompat()
            bundle.putBoolean(ARG_LOCATION, true)
            JobRequest.Builder(item.uniqueId.toString())
                    .setExact(mills)
                    .setRequiresCharging(false)
                    .setRequiresDeviceIdle(false)
                    .setRequiresBatteryNotLow(false)
                    .setRequiresStorageNotLow(false)
                    .setUpdateCurrent(true)
                    .setExtras(bundle)
                    .build()
                    .schedule()
            return true
        }

        fun enableReminder(reminder: Reminder?) {
            var due: Long = 0
            if (reminder != null) {
                due = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
            }
            LogUtil.d(TAG, "enableReminder: " + TimeUtil.getFullDateTime(due, true, true))
            if (due == 0L) {
                return
            }
            if (reminder!!.remindBefore != 0L) {
                due -= reminder.remindBefore
            }
            if (!Reminder.isBase(reminder.type, Reminder.BY_TIME)) {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = due
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                due = calendar.timeInMillis
            }
            val mills = due - System.currentTimeMillis()
            if (mills <= 0) {
                return
            }
            JobRequest.Builder(reminder.uniqueId.toString())
                    .setExact(mills)
                    .setRequiresCharging(false)
                    .setRequiresDeviceIdle(false)
                    .setRequiresBatteryNotLow(false)
                    .setRequiresStorageNotLow(false)
                    .setUpdateCurrent(true)
                    .build()
                    .schedule()
        }

        fun isEnabledReminder(id: Int): Boolean {
            return !JobManager.instance().getAllJobsForTag(id.toString()).isEmpty()
        }

        fun cancelReminder(tag: String) {
            Timber.i("cancelReminder: %s", tag)
            JobManager.instance().cancelAllForTag(tag)
        }
    }
}
