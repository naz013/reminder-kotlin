package com.elementary.tasks.core.controller

import android.content.Context
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.Prefs
import javax.inject.Inject

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
abstract class EventManager(val reminder: Reminder) : EventControl {
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var db: AppDb
    @Inject
    lateinit var prefs: Prefs
    @Inject
    lateinit var notifier: Notifier
    @Inject
    lateinit var calendarUtils: CalendarUtils

    init {
        ReminderApp.appComponent.inject(this)
    }

    protected fun save() {
        db.reminderDao().insert(reminder)
        UpdatesHelper.updateWidget(context)
        if (prefs.isSbNotificationEnabled) {
            notifier.updateReminderPermanent(PermanentReminderReceiver.ACTION_SHOW)
        }
    }

    protected fun remove() {
        db.reminderDao().delete(reminder)
        UpdatesHelper.updateWidget(context)
        if (prefs.isSbNotificationEnabled) {
            notifier.updateReminderPermanent(PermanentReminderReceiver.ACTION_SHOW)
        }
    }
}
