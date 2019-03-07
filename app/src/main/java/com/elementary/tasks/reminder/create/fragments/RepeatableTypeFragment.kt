package com.elementary.tasks.reminder.create.fragments

import androidx.databinding.ViewDataBinding
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.IntervalUtil
import com.elementary.tasks.core.utils.ReminderUtils

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
abstract class RepeatableTypeFragment<B : ViewDataBinding> : TypeFragment<B>() {

    override fun getSummary(): String {
        val reminder = iFace.state.reminder
        var summary = ""
        val groupName = reminder.groupTitle
        if (groupName != "") {
           summary += "$groupName, "
        }
        summary += ReminderUtils.getPriorityTitle(context!!, reminder.priority) + ", "
        if (reminder.remindBefore > 0) {
            summary += IntervalUtil.getBeforeTime(context!!, reminder.remindBefore) + ", "
        }
        return summary
    }

    protected fun validBefore(millis: Long, reminder: Reminder): Boolean {
        if ((millis - reminder.remindBefore - 100) < System.currentTimeMillis()) {
            return false
        }
        return true
    }

    protected fun getZeroedInt(v: Int): String {
        return if (v <= 9) {
            "0$v"
        } else {
            v.toString()
        }
    }
}
