package com.elementary.tasks.core.utils.datetime.recurrence.parser.common

import com.elementary.tasks.core.utils.datetime.recurrence.Param
import com.elementary.tasks.core.utils.datetime.recurrence.ParamType
import com.elementary.tasks.core.utils.datetime.recurrence.ParamValueType
import com.elementary.tasks.core.utils.datetime.recurrence.ValueParam

class ParamParser {

  fun parse(input: String): List<Param> {
    return input.split(";").mapNotNull { parsePart(it) }
  }

  private fun parsePart(input: String): Param? {
    if (!input.contains("=")) return null
    val parts = input.split("=")
    val type = runCatching { parseType(parts[0]) }.getOrNull()
    val value = runCatching { parseValue(parts[1]) }.getOrNull() ?: return null
    return when (type) {
      ParamType.VALUE -> ValueParam(value)
      else -> return null
    }
  }

  private fun parseType(input: String): ParamType {
    return ParamType.values().first { it.value == input }
  }

  private fun parseValue(input: String): ParamValueType {
    return ParamValueType.values().first { it.value == input }
  }
}
