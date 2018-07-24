package com.elementary.tasks.core.services

import android.content.Context
import android.content.Intent
import com.elementary.tasks.Actions

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
class PermanentBirthdayReceiver : BaseBroadcast() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (!prefs.isBirthdayPermanentEnabled) {
            notifier.hideNotification(BIRTHDAY_PERM_ID)
            return
        }
        if (intent != null) {
            val action = intent.action
            if (action != null && action.matches(ACTION_SHOW.toRegex())) {
                notifier.showBirthdayPermanent()
            } else {
                notifier.hideNotification(BIRTHDAY_PERM_ID)
            }
        } else {
            notifier.hideNotification(BIRTHDAY_PERM_ID)
        }
    }

    companion object {

        const val BIRTHDAY_PERM_ID = 356665
        const val ACTION_SHOW = Actions.Birthday.ACTION_SB_SHOW
        const val ACTION_HIDE = Actions.Birthday.ACTION_SB_HIDE
    }
}
