package com.elementary.tasks.core.calendar

import android.view.View

import com.elementary.tasks.dayView.EventsDataProvider

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
class CalendarSingleton private constructor() {
    var provider: EventsDataProvider? = null
    var fabClick: View.OnClickListener? = null

    companion object {

        private var instance: CalendarSingleton? = null

        fun getInstance(): CalendarSingleton {
            if (instance == null) {
                synchronized(CalendarSingleton::class.java) {
                    if (instance == null) {
                        instance = CalendarSingleton()
                    }
                }
            }
            return instance!!
        }
    }
}
