package com.elementary.tasks.reminder.create.fragments.recur

import com.elementary.tasks.core.utils.datetime.recurrence.FreqType
import com.elementary.tasks.core.utils.datetime.recurrence.RecurParamType

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
