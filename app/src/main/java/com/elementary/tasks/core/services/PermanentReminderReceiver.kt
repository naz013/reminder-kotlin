package com.elementary.tasks.core.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.elementary.tasks.Actions
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.Prefs

/**
 * Copyright 2017 Nazar Suhovich
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
class PermanentReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (!Prefs.getInstance(context).isSbNotificationEnabled) {
            Notifier.hideNotification(context, PERM_ID)
        }
        if (intent != null) {
            val action = intent.action
            if (action != null && action.matches(ACTION_SHOW.toRegex())) {
                Notifier.showReminderPermanent(context)
            } else {
                Notifier.hideNotification(context, PERM_ID)
            }
        } else {
            Notifier.hideNotification(context, PERM_ID)
        }
    }

    companion object {

        const val PERM_ID = 356664
        const val ACTION_SHOW = Actions.Reminder.ACTION_SB_SHOW
        const val ACTION_HIDE = Actions.Reminder.ACTION_SB_HIDE
    }
}
