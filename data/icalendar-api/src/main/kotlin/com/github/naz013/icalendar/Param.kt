package com.github.naz013.icalendar

sealed class Param : Buildable

data class ValueParam(
  val paramValueType: ParamValueType
) : Param() {

  override fun buildString(): String {
    return "${ParamType.VALUE.value}=${paramValueType.value}"
  }
}

enum class ParamType(val value: String) {
  VALUE("VALUE")
}

enum class ParamValueType(val value: String) {
  DATE_TIME("DATE-TIME"),
  DATE("DATE"),
  TIME("TIME"),
  PERIOD("PERIOD")
}
