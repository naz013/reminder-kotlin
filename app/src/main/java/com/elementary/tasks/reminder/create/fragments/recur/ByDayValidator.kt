package com.elementary.tasks.reminder.create.fragments.recur

import com.elementary.tasks.core.utils.datetime.recurrence.Day
import com.elementary.tasks.core.utils.datetime.recurrence.DayValue
import timber.log.Timber

class ByDayValidator {

  private var stateListener: ((isValid: Boolean) -> Unit)? = null
  private var lastValue: String = ""
  private var isValid: Boolean = true

  fun getValues(): List<DayValue> {
    return if (lastValue.isEmpty()) {
      emptyList()
    } else {
      lastValue.split(",").map { DayValue(it) }.also {
        Timber.d("getValues: $it")
      }
    }
  }

  fun setListener(stateListener: (isValid: Boolean) -> Unit) {
    this.stateListener = stateListener
  }

  fun onTextChanged(text: String) {
    if (text == lastValue) {
      Timber.d("onTextChanged: same")
      return
    }
    isValid = false
    lastValue = text
    if (text.matches(".*\\s.*".toRegex())) {
      Timber.d("onTextChanged: has whitespace")
      stateListener?.invoke(isValid)
      return
    }

    val values = text.split(",")

    val allContainDays = values.all { containsDay(it) }
    if (!allContainDays) {
      Timber.d("onTextChanged: not all contain day")
      stateListener?.invoke(isValid)
      return
    }

    val allCorrectPrefix = values.all { hasCorrectPrefix(it) }
    if (!allCorrectPrefix) {
      Timber.d("onTextChanged: not all have correct prefix")
      stateListener?.invoke(isValid)
      return
    }

    Timber.d("onTextChanged: is correct")

    isValid = true
    stateListener?.invoke(true)
  }

  private fun hasCorrectPrefix(value: String): Boolean {
    return days().firstOrNull { value.contains(it) }?.let { day ->
      val withoutDay = value.replace(day, "")
      if (withoutDay.isEmpty()) {
        Timber.d("hasCorrectPrefix: day = $day, no prefix")
        true
      } else {
        if (hasSuffix(value, day)) {
          false
        } else {
          val integer = runCatching { withoutDay.toInt() }.getOrNull()
          Timber.d("hasCorrectPrefix: day = $day, prefix integer = $integer")
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
    return Day.values().map { it.value }
  }
}
