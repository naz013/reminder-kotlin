package com.elementary.tasks.core.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.elementary.tasks.core.async.EnableThread
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.Prefs

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

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        LogUtil.d(TAG, "onReceive: ")
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            EnableThread(context).start()
            val alarmReceiver = AlarmReceiver()
            val prefs = Prefs.getInstance(context)
            if (prefs.isBirthdayReminderEnabled) {
                EventJobService.enableBirthdayAlarm(context)
            }
            if (prefs.isSbNotificationEnabled) {
                Notifier.updateReminderPermanent(context, PermanentReminderReceiver.ACTION_SHOW)
            }
            if (prefs.isContactAutoCheckEnabled) {
                alarmReceiver.enableBirthdayCheckAlarm(context)
            }
            if (prefs.isAutoEventsCheckEnabled) {
                alarmReceiver.enableEventCheck(context)
            }
            if (prefs.isAutoBackupEnabled) {
                alarmReceiver.enableAutoSync(context)
            }
            if (prefs.isBirthdayPermanentEnabled) {
                alarmReceiver.enableBirthdayPermanentAlarm(context)
                Notifier.showBirthdayPermanent(context)
            }
        }
    }

    companion object {

        private const val TAG = "BootReceiver"
    }
}
