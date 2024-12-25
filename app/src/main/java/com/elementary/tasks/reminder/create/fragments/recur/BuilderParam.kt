package com.elementary.tasks.reminder.create.fragments.recur

import com.github.naz013.icalendar.DayValue
import com.github.naz013.icalendar.FreqType
import com.github.naz013.icalendar.RecurParamType

data class BuilderParam<T>(
  val recurParamType: RecurParamType,
  val value: T
) {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as BuilderParam<*>

    return recurParamType == other.recurParamType
  }

  override fun hashCode(): Int {
    return recurParamType.hashCode()
  }
}

data class UiBuilderParam<T>(val text: String, val param: BuilderParam<T>)

data class UiFreqParam(val text: String, val freqType: FreqType)

data class UiDayParam(val text: String, val dayValue: DayValue)
