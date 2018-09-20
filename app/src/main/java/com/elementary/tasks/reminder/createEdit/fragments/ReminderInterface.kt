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

    val reminder: Reminder

    var canExportToTasks: Boolean

    var canExportToCalendar: Boolean

    var hasAutoExtra: Boolean

    var autoExtraHint: String

    fun selectMelody()

    fun selectExtra()

    fun selectPriority()

    fun selectLed()

    fun attachFile()

    fun selectLoudness()

    fun selectGroup()

    fun showSnackbar(title: String)

    fun showSnackbar(title: String, actionName: String, listener: View.OnClickListener)

    fun setFullScreenMode(b: Boolean)

    fun updateScroll(x: Int)
}
