package com.github.naz013.icalendar.builder

internal class LineNormalizer {

  fun normalize(lines: List<String>): List<String> {
    if (lines.isEmpty()) {
      return lines
    }
    val mutableList = mutableListOf<String>()
    lines.forEachIndexed { index, s ->
      mutableList.addAll(breakLine(s, index))
    }
    return mutableList
  }

  private fun breakLine(line: String, i: Int): List<String> {
    val mutableList = mutableListOf<String>()
    line.chunked(MAX_LENGTH).forEachIndexed { index, s ->
      if (index == 0) {
        if (i > 0) {
          mutableList.add("\n$s")
        } else {
          mutableList.add(s)
        }
      } else {
        mutableList.add("\n${getSpaces(index)}$s")
      }
    }
    return mutableList
  }

  private fun getSpaces(count: Int): String {
    return StringBuilder().apply {
      repeat(count) {
        append(" ")
      }
    }.toString()
  }

  companion object {
    private const val MAX_LENGTH = 75
  }
}
