package com.elementary.tasks.core.utils.datetime.recurrence.parser

import com.elementary.tasks.core.utils.datetime.recurrence.DateTimeEndTag
import com.elementary.tasks.core.utils.datetime.recurrence.DateTimeStampTag
import com.elementary.tasks.core.utils.datetime.recurrence.DateTimeStartTag
import com.elementary.tasks.core.utils.datetime.recurrence.DateTimeTag
import com.elementary.tasks.core.utils.datetime.recurrence.TagType
import com.elementary.tasks.core.utils.datetime.recurrence.UtcDateTime
import com.elementary.tasks.core.utils.datetime.recurrence.parser.common.UtcDateTimeParser

class DateTimeTagParser : TagParserInterface<DateTimeTag> {

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
