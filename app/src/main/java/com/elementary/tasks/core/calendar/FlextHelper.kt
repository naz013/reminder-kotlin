package com.elementary.tasks.core.calendar

import android.content.Context
import android.os.Build
import androidx.annotation.ColorRes

import java.util.Calendar
import java.util.Date

import hirondelle.date4j.DateTime

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

object FlextHelper {

    fun getColor(context: Context, @ColorRes res: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.resources.getColor(res, null)
        } else {
            context.resources.getColor(res)
        }
    }

    fun convertDateTimeToDate(dateTime: DateTime): Date {
        val year = dateTime.year!!
        val datetimeMonth = dateTime.month!!
        val day = dateTime.day!!
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(year, datetimeMonth - 1, day)
        return calendar.time
    }

    fun convertToDateTime(eventTime: Long): DateTime {
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.timeInMillis = eventTime
        var year = calendar.get(Calendar.YEAR)
        val javaMonth = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        try {
            return DateTime(year, javaMonth + 1, day, 0, 0, 0, 0)
        } catch (e: Exception) {
            calendar.timeInMillis = System.currentTimeMillis()
            year = calendar.get(Calendar.YEAR)
            try {
                return DateTime(year, javaMonth + 1, day, 0, 0, 0, 0)
            } catch (e1: Exception) {
                return DateTime(year, javaMonth + 1, day - 1, 0, 0, 0, 0)
            }

        }

    }
}
