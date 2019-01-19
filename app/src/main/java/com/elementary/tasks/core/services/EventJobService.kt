package com.elementary.tasks.core.services

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.birthdays.preview.ShowBirthdayActivity
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.utils.TimeUtil.BIRTH_FORMAT
import com.elementary.tasks.missedCalls.MissedCallDialogActivity
import com.evernote.android.job.Job
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import timber.log.Timber
import java.util.*
import javax.inject.Inject

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

    @Inject
    lateinit var prefs: Prefs

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onRunJob(params: Job.Params): Job.Result {
        Timber.d("onRunJob: %s, tag -> %s", TimeUtil.getGmtFromDateTime(System.currentTimeMillis()), params.tag)
        when (params.tag) {
            EVENT_BIRTHDAY -> birthdayAction(context)
            else -> {
                val bundle = params.extras
                when {
                    bundle.getBoolean(ARG_MISSED, false) -> {
                        openMissedScreen(params.tag)
                        enableMissedCall(prefs, params.tag)
                    }
                    bundle.getBoolean(ARG_LOCATION, false) -> SuperUtil.startGpsTracking(context)
                    else -> start(context, params.tag)
                }
            }
        }
        return Job.Result.SUCCESS
    }

    private fun birthdayAction(context: Context) {
        cancelBirthdayAlarm()
        enableBirthdayAlarm(prefs)
        Thread {
            val daysBefore = prefs.daysToBirthday
            val cal = Calendar.getInstance()
            cal.timeInMillis = System.currentTimeMillis()
            val mYear = cal.get(Calendar.YEAR)
            val mDate = BIRTH_FORMAT.format(cal.time)
            for (item in AppDb.getAppDatabase(context).birthdaysDao().all()) {
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
        return BIRTH_FORMAT.format(calendar.time)
    }

    private fun showBirthday(context: Context, item: Birthday) {
        if (prefs.reminderType == 0) {
            context.startActivity(ShowBirthdayActivity.getLaunchIntent(context, item.uuId))
        } else {
            ReminderUtils.showSimpleBirthday(context, prefs, item.uuId)
        }
    }

    private fun openMissedScreen(tag: String) {
        val resultIntent = Intent(context, MissedCallDialogActivity::class.java)
        resultIntent.putExtra(Constants.INTENT_ID, tag)
        resultIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        context.startActivity(resultIntent)
    }

    private fun start(context: Context, id: String) {
        val intent = Intent(context, ReminderActionService::class.java)
        intent.action = ReminderActionService.ACTION_RUN
        intent.putExtra(Constants.INTENT_ID, id)
        context.sendBroadcast(intent)
    }

    companion object {
        private const val EVENT_BIRTHDAY = "event_birthday"

        private const val ARG_LOCATION = "arg_location"
        private const val ARG_MISSED = "arg_missed"

        fun cancelBirthdayAlarm() {
            cancelReminder(EVENT_BIRTHDAY)
        }

        fun enableBirthdayAlarm(prefs: Prefs) {
            val time = prefs.birthdayTime
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

        internal fun enableMissedCall(prefs: Prefs, number: String?) {
            if (number == null) return
            val time = prefs.missedReminderTime
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

        fun enableDelay(time: Int, id: String) {
            val min = TimeCount.MINUTE
            val due = System.currentTimeMillis() + min * time
            val mills = due - System.currentTimeMillis()
            if (due == 0L || mills <= 0) {
                return
            }
            JobRequest.Builder(id)
                    .setExact(mills)
                    .setRequiresCharging(false)
                    .setRequiresDeviceIdle(false)
                    .setRequiresBatteryNotLow(false)
                    .setRequiresStorageNotLow(false)
                    .setUpdateCurrent(true)
                    .build()
                    .schedule()
        }

        fun enablePositionDelay(context: Context, id: String): Boolean {
            val item = AppDb.getAppDatabase(context).reminderDao().getById(id) ?: return false
            val due = TimeUtil.getDateTimeFromGmt(item.eventTime)
            val mills = due - System.currentTimeMillis()
            if (due == 0L || mills <= 0) {
                return false
            }
            val bundle = PersistableBundleCompat()
            bundle.putBoolean(ARG_LOCATION, true)
            JobRequest.Builder(item.uuId)
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
            if (reminder == null) return
            var due = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
            Timber.d("enableReminder: ${TimeUtil.getFullDateTime(due, true)}")
            Timber.d("enableReminder: noe -> ${TimeUtil.getFullDateTime(System.currentTimeMillis(), true)}")
            if (due == 0L) {
                return
            }
            if (reminder.remindBefore != 0L) {
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
            JobRequest.Builder(reminder.uuId)
                    .setExact(mills)
                    .setRequiresCharging(false)
                    .setRequiresDeviceIdle(false)
                    .setRequiresBatteryNotLow(false)
                    .setRequiresStorageNotLow(false)
                    .setUpdateCurrent(true)
                    .build()
                    .schedule()
        }

        fun isEnabledReminder(id: String): Boolean {
            return !JobManager.instance().getAllJobsForTag(id).isEmpty()
        }

        fun cancelReminder(tag: String) {
            Timber.i("cancelReminder: $tag")
            JobManager.instance().cancelAllForTag(tag)
        }
    }
}
