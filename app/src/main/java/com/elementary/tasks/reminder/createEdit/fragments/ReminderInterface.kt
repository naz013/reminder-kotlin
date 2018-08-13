package com.elementary.tasks.reminder.createEdit.fragments

import android.view.View
import com.elementary.tasks.core.data.models.ReminderGroup

import com.elementary.tasks.core.data.models.Reminder

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

interface ReminderInterface {
    val reminder: Reminder?

    val useGlobal: Boolean

    val voice: Boolean

    val vibration: Boolean

    val notificationRepeat: Boolean

    val wake: Boolean

    val unlock: Boolean

    val auto: Boolean

    val volume: Int

    val ledColor: Int

    var repeatLimit: Int

    val windowType: Int

    val reminderGroup: ReminderGroup?

    val isExportToCalendar: Boolean

    val isExportToTasks: Boolean

    val summary: String

    val attachment: String

    val melodyPath: String

    fun setEventHint(hint: String)

    fun showSnackbar(title: String)

    fun showSnackbar(title: String, actionName: String, listener: View.OnClickListener)

    fun setExclusionAction(listener: View.OnClickListener?)

    fun setRepeatAction(listener: View.OnClickListener?)

    fun setFullScreenMode(b: Boolean)

    fun setHasAutoExtra(b: Boolean, label: String)
}
