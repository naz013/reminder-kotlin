package com.elementary.tasks.core.appWidgets.events

import android.app.IntentService
import android.content.Intent
import com.elementary.tasks.birthdays.create.AddBirthdayActivity
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.reminder.create.CreateReminderActivity
import timber.log.Timber

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
class EventEditService : IntentService("EventEditService") {

    override fun onHandleIntent(intent: Intent?) {
        val id = intent?.getIntExtra(Constants.INTENT_ID, 0) ?: 0
        val isReminder = intent?.getBooleanExtra(TYPE, true) ?: true
        Timber.d("onHandleIntent: $id, isReminder $isReminder")
        if (id != 0) {
            if (isReminder) {
                CreateReminderActivity.openLogged(applicationContext,
                        Intent(applicationContext, CreateReminderActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .putExtra(Constants.INTENT_ID, id))
            } else {
                AddBirthdayActivity.openLogged(applicationContext,
                        Intent(applicationContext, AddBirthdayActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .putExtra(Constants.INTENT_ID, id))
            }
        }
        stopSelf()
    }

    companion object {
        const val TYPE = "type"
    }
}
