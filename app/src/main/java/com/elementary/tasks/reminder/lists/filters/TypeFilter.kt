package com.elementary.tasks.reminder.lists.filters

import com.elementary.tasks.core.data.models.Reminder

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
class TypeFilter(filter: ObjectFilter<Reminder>?) : AbstractFilter<Int, Reminder>(filter) {

    private var type = 0

    override fun filter(reminder: Reminder): Boolean {
        return super.filter(reminder) && (type == 0 || reminder.type == type)
    }

    @Throws(Exception::class)
    override fun accept(integer: Int?) {
        this.type = integer!!
    }
}
