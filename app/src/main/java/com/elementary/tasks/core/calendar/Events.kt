package com.elementary.tasks.core.calendar

class Events {
    private var events = mutableListOf<Event>()
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
