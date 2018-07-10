package com.elementary.tasks.core.utils

import android.content.Context

import com.elementary.tasks.R

import java.util.ArrayList

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

object IntervalUtil {

    val REPEAT_CODE_ONCE = 0
    val INTERVAL_DAY = 1
    val INTERVAL_WEEK = INTERVAL_DAY * 7
    val INTERVAL_TWO_WEEKS = INTERVAL_WEEK * 2
    val INTERVAL_THREE_WEEKS = INTERVAL_WEEK * 3
    val INTERVAL_FOUR_WEEKS = INTERVAL_WEEK * 4

    fun getWeekRepeat(mon: Boolean, tue: Boolean, wed: Boolean, thu: Boolean, fri: Boolean,
                      sat: Boolean, sun: Boolean): List<Int> {
        val sb = ArrayList<Int>(7)
        sb.add(0, if (sun) 1 else 0)
        sb.add(1, if (mon) 1 else 0)
        sb.add(2, if (tue) 1 else 0)
        sb.add(3, if (wed) 1 else 0)
        sb.add(4, if (thu) 1 else 0)
        sb.add(5, if (fri) 1 else 0)
        sb.add(6, if (sat) 1 else 0)
        return sb
    }

    fun isWeekday(weekday: List<Int>?): Boolean {
        if (weekday == null) return false
        for (day in weekday) {
            if (day == ReminderUtils.DAY_CHECKED) {
                return true
            }
        }
        return false
    }

    fun getInterval(mContext: Context, code: Long): String {
        var code = code
        val minute = (1000 * 60).toLong()
        val day = minute * 60 * 24
        val tmp = code / minute
        val interval: String
        if (tmp > 1000) {
            code /= day
            if (code == REPEAT_CODE_ONCE.toLong()) {
                interval = "0"
            } else if (code == INTERVAL_WEEK.toLong()) {
                interval = String.format(mContext.getString(R.string.xW), 1.toString())
            } else if (code == INTERVAL_TWO_WEEKS.toLong()) {
                interval = String.format(mContext.getString(R.string.xW), 2.toString())
            } else if (code == INTERVAL_THREE_WEEKS.toLong()) {
                interval = String.format(mContext.getString(R.string.xW), 3.toString())
            } else if (code == INTERVAL_FOUR_WEEKS.toLong()) {
                interval = String.format(mContext.getString(R.string.xW), 4.toString())
            } else {
                interval = String.format(mContext.getString(R.string.xD), code.toString())
            }
        } else {
            return if (tmp == 0L) {
                "0"
            } else {
                String.format(mContext.getString(R.string.x_min), tmp.toString())
            }
        }
        return interval
    }
}
