package com.elementary.tasks.day_view.day

import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.TimeUtil

/**
 * Copyright 2018 Nazar Suhovich
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
data class EventModel(
        val viewType: Int,
        var model: Any,
        val day: Int,
        val month: Int,
        val year: Int,
        val dt: Long,
        val color: Int
) {
    fun getMillis(time: Long = 0): Long {
        val o = model
        return if (o is Reminder) {
            TimeUtil.getDateTimeFromGmt(o.eventTime)
        } else if (o is Birthday) {
            TimeUtil.getFutureBirthdayDate(time, o.date)?.millis ?: 0L
        } else {
            0
        }
    }

    companion object {
        const val REMINDER = 0
        const val BIRTHDAY = 2
    }
}