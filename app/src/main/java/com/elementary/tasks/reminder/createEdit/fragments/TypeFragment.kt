package com.elementary.tasks.reminder.createEdit.fragments

import android.app.Activity
import android.content.Context

import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder

import androidx.fragment.app.Fragment
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.utils.TimeCount
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

    var reminderInterface: ReminderInterface? = null
        private set

    @Inject
    lateinit var prefs: Prefs
    @Inject
    lateinit var dialogues: Dialogues
    @Inject
    lateinit var themeUtil: ThemeUtil
    @Inject
    lateinit var timeCount: TimeCount

    init {
        ReminderApp.appComponent.inject(this)
    }

    abstract fun prepare(): Reminder?

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (reminderInterface == null) {
            reminderInterface = context as ReminderInterface?
            setDefault()
        }
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        if (reminderInterface == null) {
            reminderInterface = activity as ReminderInterface?
            setDefault()
        }
    }

    private fun setDefault() {
        reminderInterface?.setExclusionAction(null)
        reminderInterface?.setRepeatAction(null)
        reminderInterface?.setEventHint(getString(R.string.remind_me))
        reminderInterface?.setHasAutoExtra(false, "")
    }

    open fun onBackPressed(): Boolean {
        return true
    }
}
