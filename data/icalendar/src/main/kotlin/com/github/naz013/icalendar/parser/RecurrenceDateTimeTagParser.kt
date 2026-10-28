package com.github.naz013.icalendar.parser

import com.github.naz013.icalendar.RecurrenceDateTimeTag
import com.github.naz013.icalendar.TagType
import com.github.naz013.icalendar.UtcDateTime
import com.github.naz013.icalendar.ValueParam
import com.github.naz013.icalendar.parser.common.ArrayParser
import com.github.naz013.icalendar.parser.common.UtcDateTimeParser
import com.github.naz013.icalendar.parser.common.ParamParser
import com.github.naz013.icalendar.parser.common.TimeZoneIdParser

internal class RecurrenceDateTimeTagParser : TagParserInterface<RecurrenceDateTimeTag> {

  private val arrayParser = ArrayParser()
  private val utcDateTimeParser = UtcDateTimeParser()
  private val paramParser = ParamParser()
  private val timeZoneIdParser = TimeZoneIdParser()

  override fun parse(line: String): RecurrenceDateTimeTag? {
    return if (line.startsWith(TagType.RDATE.value)) {
      val parts = line.split(":")
      val param = runCatching { parseParam(parts[0]) }.getOrNull()
      val timeZoneId = timeZoneIdParser.parse(parts[0])
      runCatching { parseDateTimes(parts[1]) }.getOrNull()?.let { RecurrenceDateTimeTag(param, it, timeZoneId) }
    } else {
      null
    }
  }

  private fun parseParam(input: String): ValueParam? {
    return paramParser.parse(input).firstOrNull()
      ?.takeIf { it is ValueParam }
      ?.let { it as? ValueParam }
  }

  private fun parseDateTimes(input: String): List<UtcDateTime> {
    return arrayParser.parse(input).mapNotNull { utcDateTimeParser.parse(it) }
  }
}
