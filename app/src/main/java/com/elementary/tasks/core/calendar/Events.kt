package com.elementary.tasks.core.calendar

import java.util.ArrayList
import kotlin.Comparator

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
class Events {
    private var events: ArrayList<Event> = ArrayList()
    private var mPosition = 0

    val nextWithoutMoving: Event?
        get() {
            val index = mPosition + 1
            return if (index < events.size) {
                events[index]
            } else
                null
        }

    val previousWithoutMoving: Event?
        get() {
            if (mPosition == 0) return null
            val index = mPosition - 1
            return if (index < events.size) {
                events[index]
            } else
                null
        }

    val next: Event?
        get() {
            return if (mPosition < events.size) {
                val event = events[mPosition]
                mPosition++
                event
            } else
                null
        }

    val last: Event?
        get() = if (events.isNotEmpty()) {
            events[events.size - 1]
        } else
            null

    constructor() {
        events.clear()
    }

    constructor(event: Event): this() {
        events.add(event)
    }

    constructor(task: String, color: Int, type: Type, time: Long): this() {
        val event = Event(task, color, type, time)
        events.add(event)
        events.sortWith(Comparator { event1, t1 -> (event1.time - t1.time).toInt() })
    }

    fun moveToStart() {
        mPosition = 0
    }

    fun addEvent(task: String, color: Int, type: Type, time: Long): Int {
        val event = Event(task, color, type, time)
        events.add(event)
        return events.indexOf(event)
    }

    operator fun hasNext(): Boolean {
        return mPosition < events.size
    }

    fun count(): Int {
        return events.size
    }

    override fun toString(): String {
        return events.toString()
    }

    enum class Type {
        REMINDER,
        BIRTHDAY
    }

    data class Event(var task: String?, var color: Int, var type: Type?, var time: Long)
}
