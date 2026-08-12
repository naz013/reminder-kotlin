package com.github.naz013.icalendar.parser

import com.github.naz013.icalendar.ExceptionsDateTimeTag
import com.github.naz013.icalendar.TagType
import com.github.naz013.icalendar.UtcDateTime
import com.github.naz013.icalendar.parser.common.ArrayParser
import com.github.naz013.icalendar.parser.common.UtcDateTimeParser

internal class ExceptionsDateTimeTagParser : TagParserInterface<ExceptionsDateTimeTag> {

  private val arrayParser = ArrayParser()
  private val utcDateTimeParser = UtcDateTimeParser()

  override fun parse(line: String): ExceptionsDateTimeTag? {
    return if (line.startsWith(TagType.EXDATE.value)) {
      val parts = line.split(":")
      runCatching { parseDateTimes(parts[1]) }.getOrNull()?.let { ExceptionsDateTimeTag(it) }
    } else {
      null
    }
  }

  private fun parseDateTimes(input: String): List<UtcDateTime> {
    return arrayParser.parse(input).mapNotNull { utcDateTimeParser.parse(it) }
  }
}
