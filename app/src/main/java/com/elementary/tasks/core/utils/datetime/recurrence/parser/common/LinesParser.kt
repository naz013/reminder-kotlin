package com.elementary.tasks.core.utils.datetime.recurrence.parser.common

class LinesParser {

  fun parse(input: String): List<String> {
    if (input.isEmpty()) return emptyList()
    val mutableList = mutableListOf<String>()
    val stringBuilder = StringBuilder()
    input.split("\n").forEach { line ->
      if (line.startsWith(" ")) {
        stringBuilder.append(line.trimStart())
      } else {
        if (stringBuilder.isNotEmpty()) {
          mutableList.add(stringBuilder.toString())
          stringBuilder.clear()
        }
        stringBuilder.append(line.trimStart())
      }
    }
    if (stringBuilder.isNotEmpty()) {
      mutableList.add(stringBuilder.toString())
      stringBuilder.clear()
    }
    return mutableList
  }
}
