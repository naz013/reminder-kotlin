package com.backdoor.engine.lang

internal fun String.matches(s: String) = matches(s.toRegex())

internal fun String?.matchesOrFalse(s: String) = this?.matches(s.toRegex()) ?: false

internal fun String.splitByWhitespace() = split(Worker.WHITESPACE)

internal fun String.splitByWhitespaces() = split(Worker.WHITESPACES)

internal fun String.trim() = trim { it <= ' ' }

internal fun String.toRepeat(def1: Long, def2: Long = 0): Long {
  return try {
    toLong()
  } catch (e: NumberFormatException) {
    def1
  } catch (e: ArrayIndexOutOfBoundsException) {
    def2
  }.let { it * Worker.DAY }
}

internal fun List<String>.clip() =
  joinToString(" ").trim().replace("\\s{2,}".toRegex(), " ")