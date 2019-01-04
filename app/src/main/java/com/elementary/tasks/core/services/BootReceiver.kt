package com.elementary.tasks.core.services

import android.content.Context
import android.content.Intent
import com.elementary.tasks.core.utils.EnableThread
import timber.log.Timber

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
class BootReceiver : BaseBroadcast() {

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("onReceive: ")
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            EnableThread.run(context)
            val alarmReceiver = AlarmReceiver()
            if (prefs.isBirthdayReminderEnabled) {
                EventJobService.enableBirthdayAlarm(prefs)
            }
            if (prefs.isSbNotificationEnabled) {
                notifier.updateReminderPermanent(PermanentReminderReceiver.ACTION_SHOW)
            }
            if (prefs.isContactAutoCheckEnabled) {
                alarmReceiver.enableBirthdayCheckAlarm()
            }
            if (prefs.isAutoEventsCheckEnabled) {
                alarmReceiver.enableEventCheck(context)
            }
            if (prefs.isBackupEnabled && prefs.isAutoBackupEnabled) {
                alarmReceiver.enableAutoSync(context)
            }
            if (prefs.isBirthdayPermanentEnabled) {
                alarmReceiver.enableBirthdayPermanentAlarm(context)
                notifier.showBirthdayPermanent()
            }
        }
    }
}
