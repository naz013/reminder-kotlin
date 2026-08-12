package com.github.naz013.icalendar.parser.common

import com.github.naz013.icalendar.Param
import com.github.naz013.icalendar.ParamType
import com.github.naz013.icalendar.ParamValueType
import com.github.naz013.icalendar.ValueParam

internal class ParamParser {

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
