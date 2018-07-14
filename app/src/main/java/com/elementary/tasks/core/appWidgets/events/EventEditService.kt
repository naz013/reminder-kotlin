package com.elementary.tasks.core.appWidgets.events

import android.app.IntentService
import android.content.Intent

import com.elementary.tasks.birthdays.createEdit.AddBirthdayActivity
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.reminder.create_edit.CreateReminderActivity

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
        val id = intent!!.getStringExtra(Constants.INTENT_ID)
        val isReminder = intent.getBooleanExtra(TYPE, true)
        LogUtil.d(TAG, "onHandleIntent: $id isReminder $isReminder")
        if (id != null) {
            if (isReminder) {
                startActivity(Intent(applicationContext, CreateReminderActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(Constants.INTENT_ID, id))
            } else {
                startActivity(Intent(applicationContext, AddBirthdayActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(Constants.INTENT_ID, id))
            }
        }
        stopSelf()
    }

    companion object {

        private const val TAG = "EventEditService"
        const val TYPE = "type"
    }
}
