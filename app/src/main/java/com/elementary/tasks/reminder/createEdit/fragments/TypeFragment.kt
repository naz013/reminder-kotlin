package com.elementary.tasks.reminder.createEdit.fragments

import android.content.Context
import androidx.fragment.app.Fragment
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.*
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
abstract class TypeFragment : Fragment() {

    lateinit var reminderInterface: ReminderInterface
        private set

    @Inject
    lateinit var prefs: Prefs
    @Inject
    lateinit var dialogues: Dialogues
    @Inject
    lateinit var themeUtil: ThemeUtil
    @Inject
    lateinit var timeCount: TimeCount
    @Inject
    lateinit var reminderUtils: ReminderUtils

    init {
        ReminderApp.appComponent.inject(this)
    }

    abstract fun prepare(): Reminder?

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        reminderInterface = context as ReminderInterface
    }

    open fun getSummary(): String {
        return ""
    }

    open fun onBackPressed(): Boolean {
        return true
    }

    open fun onMelodySelect(path: String) {
        reminderInterface.reminder.melodyPath = path
    }

    open fun onVoiceAction(text: String) {

    }

    open fun onAttachmentSelect(path: String) {
        reminderInterface.reminder.attachmentFile = path
    }

    open fun onGroupUpdate(reminderGroup: ReminderGroup) {
        val reminder = reminderInterface.reminder
        reminder.groupUuId = reminderGroup.groupUuId
        reminder.groupColor = reminderGroup.groupColor
        reminder.groupTitle = reminderGroup.groupTitle
    }

    override fun onResume() {
        super.onResume()
        if (reminderInterface.reminder.groupUuId == "") {
            val defGroup = reminderInterface.defGroup ?: return
            onGroupUpdate(defGroup)
        }
    }
}