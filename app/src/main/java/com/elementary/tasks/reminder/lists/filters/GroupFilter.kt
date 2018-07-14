package com.elementary.tasks.reminder.lists.filters

import com.elementary.tasks.core.data.models.Reminder

import java.util.ArrayList

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
class GroupFilter(filter: ObjectFilter<Reminder>?) : AbstractFilter<List<String>, Reminder>(filter) {

    private var groupIds: List<String> = ArrayList()

    override fun filter(reminder: Reminder): Boolean {
        if (!super.filter(reminder)) return false
        if (groupIds.isEmpty()) return true
        for (s in groupIds) {
            if (reminder.groupUuId == s) return true
        }
        return false
    }

    @Throws(Exception::class)
    override fun accept(s: List<String>) {
        this.groupIds = s
    }
}