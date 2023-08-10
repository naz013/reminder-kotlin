package com.elementary.tasks.core.calendar

import org.threeten.bp.LocalDate

class EventsCursor {
  private var events = mutableListOf<Event>()
  private var mPosition = 0

  val nextWithoutMoving: Event?
    get() {
      val index = mPosition + 1
      return if (index < events.size) {
        events[index]
      } else {
        null
      }
    }

  val previousWithoutMoving: Event?
    get() {
      if (mPosition == 0) {
        return null
      }
      val index = mPosition - 1
      return if (index < events.size) {
        events[index]
      } else {
        null
      }
    }

  val next: Event?
    get() {
      return if (mPosition < events.size) {
        val event = events[mPosition]
        mPosition++
        event
      } else {
        null
      }
    }

  val last: Event?
    get() = if (events.isNotEmpty()) {
      events[events.size - 1]
    } else {
      null
    }

  constructor() {
    events.clear()
  }

  constructor(task: String, color: Int, type: Type, date: LocalDate) : this() {
    val event = Event(task, color, type, date)
    events.add(event)
    val sorted = events.sortedBy { it.date }
    events.clear()
    events.addAll(sorted)
  }

  fun moveToStart() {
    mPosition = 0
  }

  fun addEvent(task: String, color: Int, type: Type, date: LocalDate): Int {
    val event = Event(task, color, type, date)
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

  data class Event(var task: String?, var color: Int, var type: Type?, var date: LocalDate)
}
