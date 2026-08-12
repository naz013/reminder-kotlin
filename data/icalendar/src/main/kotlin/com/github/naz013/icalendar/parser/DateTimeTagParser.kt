package com.github.naz013.icalendar.parser

import com.github.naz013.icalendar.DateTimeEndTag
import com.github.naz013.icalendar.DateTimeStampTag
import com.github.naz013.icalendar.DateTimeStartTag
import com.github.naz013.icalendar.DateTimeTag
import com.github.naz013.icalendar.TagType
import com.github.naz013.icalendar.UtcDateTime
import com.github.naz013.icalendar.parser.common.UtcDateTimeParser

internal class DateTimeTagParser : TagParserInterface<DateTimeTag> {

  private val utcDateTimeParser = UtcDateTimeParser()

  override fun parse(line: String): DateTimeTag? {
    return if (isDateTimeTag(line)) {
      val parts = line.split(":")
      runCatching { parseDateTime(parts[1]) }.getOrNull()?.let { createTag(line, it) }
    } else {
      null
    }
  }

  private fun createTag(line: String, utcDateTime: UtcDateTime): DateTimeTag? {
    return when {
      line.startsWith(TagType.DTSTART.value) -> DateTimeStartTag(utcDateTime)
      line.startsWith(TagType.DTEND.value) -> DateTimeEndTag(utcDateTime)
      line.startsWith(TagType.DTSTAMP.value) -> DateTimeStampTag(utcDateTime)
      else -> null
    }
  }

  private fun isDateTimeTag(line: String): Boolean {
    return line.startsWith(TagType.DTSTART.value) || line.startsWith(TagType.DTEND.value) ||
      line.startsWith(TagType.DTSTAMP.value)
  }

  private fun parseDateTime(input: String): UtcDateTime? {
    return utcDateTimeParser.parse(input)
  }
}
