package com.elementary.tasks.reminder.create.fragments.recur

import com.github.naz013.icalendar.Day
import com.github.naz013.icalendar.DayValue
import com.github.naz013.logging.Logger

class ByDayValidator {

  private var stateListener: ((isValid: Boolean) -> Unit)? = null
  private var lastValue: String = ""
  private var isValid: Boolean = true

  fun getValues(): List<DayValue> {
    return if (lastValue.isEmpty()) {
      emptyList()
    } else {
      lastValue.split(",").map { DayValue(it) }.also {
        Logger.d("getValues: $it")
      }
    }
  }

  fun setListener(stateListener: (isValid: Boolean) -> Unit) {
    this.stateListener = stateListener
  }

  fun onTextChanged(text: String) {
    if (text == lastValue) {
      Logger.d("onTextChanged: same")
      return
    }
    isValid = false
    lastValue = text
    if (text.matches(".*\\s.*".toRegex())) {
      Logger.d("onTextChanged: has whitespace")
      stateListener?.invoke(isValid)
      return
    }

    val values = text.split(",")

    val allContainDays = values.all { containsDay(it) }
    if (!allContainDays) {
      Logger.d("onTextChanged: not all contain day")
      stateListener?.invoke(isValid)
      return
    }

    val allCorrectPrefix = values.all { hasCorrectPrefix(it) }
    if (!allCorrectPrefix) {
      Logger.d("onTextChanged: not all have correct prefix")
      stateListener?.invoke(isValid)
      return
    }

    Logger.d("onTextChanged: is correct")

    isValid = true
    stateListener?.invoke(true)
  }

  private fun hasCorrectPrefix(value: String): Boolean {
    return days().firstOrNull { value.contains(it) }?.let { day ->
      val withoutDay = value.replace(day, "")
      if (withoutDay.isEmpty()) {
        Logger.d("hasCorrectPrefix: day = $day, no prefix")
        true
      } else {
        if (hasSuffix(value, day)) {
          false
        } else {
          val integer = runCatching { withoutDay.toInt() }.getOrNull()
          Logger.d("hasCorrectPrefix: day = $day, prefix integer = $integer")
          integer != null
        }
      }
    } ?: false
  }

  private fun hasSuffix(value: String, dayValue: String): Boolean {
    val index = value.indexOf(dayValue)
    return if (index != -1) {
      index + dayValue.length < value.length
    } else {
      false
    }
  }

  private fun containsDay(value: String): Boolean {
    return days().any { value.contains(it) }
  }

  private fun days(): List<String> {
    return Day.entries.map { it.value }
  }
}
